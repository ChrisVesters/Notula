import Session from "$lib/auth/Session";
import CLIENT_ID from "$lib/common/ClientId";
import { isOwnEvent } from "$lib/common/EventTypes";
import type WebSocketClient from "$lib/common/WebSocketClient";
import type { MeetingDetails } from "$lib/details/DetailTypes";
import { composed, rebased } from "$lib/editor/TextEdit";

import type {
	Acknowledged,
	Change,
	Rejected,
	Submission,
	TextEdit
} from "./change/ChangeTypes";
import type { MeetingEvent, Mutation } from "./event/EventTypes";

export type MeetingEventHandler = {
	onLoad: (data: MeetingDetails) => void;
	onEvent: (event: MeetingEvent) => void;
	onRejected: (rejected: Rejected) => void;
};

export default class MeetingWebSocketClient {
	static readonly #REJECTIONS = "/user/queue/rejections";
	static readonly #ACKS = "/user/queue/acks";

	static #meetingId: number | null = null;
	static #handler: MeetingEventHandler | null = null;

	static #revision: number | undefined = undefined;
	static #streamed = false;
	static #buffered: Array<MeetingEvent> = [];

	static #inFlight: Submission | null = null;
	// The revision the change in flight committed at, once acknowledged. The
	// next change names the revision it was written against, and the page only
	// holds everything up to this one once its event has been applied. The
	// acknowledgement and the event come on different subscriptions, so either
	// can arrive first.
	static #acknowledged: number | null = null;
	// Whether the change in flight is in the page but not yet in a revision
	// the page has applied. Until its echo arrives, a remote edit to the same
	// text was written without it and has to be moved over it.
	static #pending = false;
	static #queued: Array<Submission> = [];

	public static connect(id: number, handler: MeetingEventHandler): void {
		MeetingWebSocketClient.#meetingId = id;
		MeetingWebSocketClient.#handler = handler;

		client().subscribe(MeetingWebSocketClient.#ACKS, message =>
			MeetingWebSocketClient.#acknowledge(JSON.parse(message.body))
		);
		client().subscribe(MeetingWebSocketClient.#REJECTIONS, message => {
			const rejected: Rejected = JSON.parse(message.body);

			// The page already shows the refused change and whatever was
			// written after it, none of which the server will ever hold, so
			// every edit from here on would name positions in text it does not
			// have. Only a snapshot puts the page back on the server's text.
			MeetingWebSocketClient.#clear();
			MeetingWebSocketClient.resync();
			handler.onRejected(rejected);
		});
		client().subscribe(`/topic/meetings/${id}`, message =>
			MeetingWebSocketClient.#accept(JSON.parse(message.body))
		);
		MeetingWebSocketClient.#load(id);
	}

	public static resync(): void {
		const id = MeetingWebSocketClient.#meetingId;
		if (id === null) {
			throw new Error("No meeting to reload");
		}

		if (MeetingWebSocketClient.#revision === undefined) {
			return;
		}

		console.warn("Missed a change, reloading the meeting");
		MeetingWebSocketClient.#revision = undefined;
		MeetingWebSocketClient.#streamed = false;

		client().unsubscribe(`/app/meetings/${id}`);
		MeetingWebSocketClient.#load(id);
	}

	public static disconnect(): void {
		const id = MeetingWebSocketClient.#meetingId;
		MeetingWebSocketClient.#meetingId = null;
		MeetingWebSocketClient.#handler = null;
		MeetingWebSocketClient.#revision = undefined;
		MeetingWebSocketClient.#streamed = false;
		MeetingWebSocketClient.#buffered = [];
		MeetingWebSocketClient.#clear();

		client().unsubscribe(`/app/meetings/${id}`);
		client().unsubscribe(`/topic/meetings/${id}`);
		client().unsubscribe(MeetingWebSocketClient.#REJECTIONS);
		client().unsubscribe(MeetingWebSocketClient.#ACKS);
	}

	public static send(change: Change): string {
		if (MeetingWebSocketClient.#meetingId === null) {
			throw new Error("No meeting to change");
		}

		const waiting = MeetingWebSocketClient.#merge(change);
		if (waiting !== null) {
			return waiting.id;
		}

		const id: string = crypto.randomUUID();

		MeetingWebSocketClient.#queued.push({ id, change });
		MeetingWebSocketClient.#submit();

		return id;
	}

	// Keystrokes typed while a change is in flight join the text edit waiting
	// behind it, so a fast typist sends one change per round trip rather than
	// one per character. Only the last one waiting: merging into an earlier one
	// would move the edit ahead of the changes queued after it.
	static #merge(change: Change): Submission | null {
		const last = MeetingWebSocketClient.#queued.at(-1);
		const target = textOf(change);
		if (last === undefined || target === null) {
			return null;
		}

		if (textOf(last.change) !== target) {
			return null;
		}

		const edit = composed(editOf(last.change), editOf(change));
		if (edit === null) {
			return null;
		}

		last.change = { ...last.change, ...edit } as Change;
		return last;
	}

	static #onLoad(data: MeetingDetails): void {
		// The snapshot replaces the page, and with it every edit that was
		// waiting to go out: they were written against text that is no longer
		// on screen. The one in flight is no longer on screen either, so its
		// echo has to be applied like anyone else's.
		if (MeetingWebSocketClient.#queued.length > 0) {
			console.warn("Dropping unsent changes for the reloaded meeting");
		}
		MeetingWebSocketClient.#queued = [];
		MeetingWebSocketClient.#pending = false;

		MeetingWebSocketClient.#revision = data.revision;
		MeetingWebSocketClient.#streamed = false;
		MeetingWebSocketClient.#handler?.onLoad(data);

		MeetingWebSocketClient.#buffered
			.splice(0, MeetingWebSocketClient.#buffered.length)
			.forEach(event => MeetingWebSocketClient.#accept(event));

		MeetingWebSocketClient.#release();
		MeetingWebSocketClient.#submit();
	}

	static #accept(event: MeetingEvent): void {
		const revision = MeetingWebSocketClient.#revision;
		if (revision === undefined) {
			MeetingWebSocketClient.#buffered.push(event);
			return;
		}

		const seen =
			event.revision < revision ||
			(event.revision === revision && !MeetingWebSocketClient.#streamed);
		if (seen) {
			return;
		}

		if (event.revision > revision + 1) {
			MeetingWebSocketClient.resync();
			return;
		}

		MeetingWebSocketClient.#revision = event.revision;
		MeetingWebSocketClient.#streamed = true;

		MeetingWebSocketClient.#receive(event);
		MeetingWebSocketClient.#release();
	}

	// Our own changes come back too. Creating, moving and deleting still
	// relies on that echo to update the view, so it is handed on. Text is the
	// exception: the editor already applied it, and applying the echo would
	// splice the same edit in twice.
	static #receive(event: MeetingEvent): void {
		const echo =
			isOwnEvent(event.origin) && MeetingWebSocketClient.#pending;
		if (echo) {
			MeetingWebSocketClient.#pending = false;

			if (textOf(event.mutation) === null) {
				MeetingWebSocketClient.#handler?.onEvent(event);
			}
			return;
		}

		MeetingWebSocketClient.#handler?.onEvent(
			MeetingWebSocketClient.#rebase(event)
		);
	}

	// A remote edit was written without the edits this page has made and the
	// server has not yet put in a revision, and those were written without
	// it. Each is moved over the other, so the page applies the remote edit
	// where it now belongs, and what is still to be sent names positions the
	// server will agree with.
	static #rebase(event: MeetingEvent): MeetingEvent {
		const target = textOf(event.mutation);
		if (target === null) {
			return event;
		}

		const inFlight = MeetingWebSocketClient.#inFlight;
		const local = [
			...(MeetingWebSocketClient.#pending && inFlight !== null
				? [inFlight]
				: []),
			...MeetingWebSocketClient.#queued
		];

		let incoming = editOf(event.mutation);
		local
			.filter(submission => textOf(submission.change) === target)
			.forEach(submission => {
				const own = editOf(submission.change);

				submission.change = {
					...submission.change,
					...rebased(own, incoming)
				} as Change;
				incoming = rebased(incoming, own, true);
			});

		return {
			...event,
			mutation: { ...event.mutation, ...incoming } as Mutation
		};
	}

	static #acknowledge(acknowledged: Acknowledged): void {
		if (MeetingWebSocketClient.#inFlight?.id !== acknowledged.id) {
			return;
		}

		MeetingWebSocketClient.#acknowledged = acknowledged.revision;
		MeetingWebSocketClient.#release();
	}

	static #release(): void {
		const acknowledged = MeetingWebSocketClient.#acknowledged;
		const revision = MeetingWebSocketClient.#revision;
		if (
			acknowledged === null ||
			revision === undefined ||
			revision < acknowledged
		) {
			return;
		}

		MeetingWebSocketClient.#inFlight = null;
		MeetingWebSocketClient.#acknowledged = null;
		MeetingWebSocketClient.#pending = false;
		MeetingWebSocketClient.#submit();
	}

	static #submit(): void {
		const meetingId = MeetingWebSocketClient.#meetingId;
		const revision = MeetingWebSocketClient.#revision;
		if (
			meetingId === null ||
			revision === undefined ||
			MeetingWebSocketClient.#inFlight !== null
		) {
			return;
		}

		const next = MeetingWebSocketClient.#queued.shift();
		if (next === undefined) {
			return;
		}

		// Stamped as it leaves rather than when it was written: an edit
		// waiting in the queue was typed after the one ahead of it, so its
		// positions already hold that one, and the revision that committed it
		// is the base they were counted in.
		const body =
			textOf(next.change) !== null
				? { ...next.change, base: revision }
				: next.change;

		MeetingWebSocketClient.#inFlight = next;
		MeetingWebSocketClient.#pending = true;
		client().send(
			`/app/meetings/${meetingId}/changes`,
			JSON.stringify(body),
			{ "client-id": CLIENT_ID, "change-id": next.id }
		);
	}

	static #clear(): void {
		MeetingWebSocketClient.#inFlight = null;
		MeetingWebSocketClient.#acknowledged = null;
		MeetingWebSocketClient.#pending = false;
		MeetingWebSocketClient.#queued = [];
	}

	static #load(id: number): void {
		client().subscribe(`/app/meetings/${id}`, message =>
			MeetingWebSocketClient.#onLoad(JSON.parse(message.body))
		);
	}
}

// Names the text an edit changes, so two edits to the same text compare
// equal whichever half of the wire they were read from.
function textOf(item: Change | Mutation): string | null {
	switch (item.type) {
		case "RENAME_MEETING":
		case "DESCRIBE_MEETING":
			return item.type;
		case "RENAME_TOPIC":
		case "DESCRIBE_TOPIC":
			return `${item.type}:${item.topic}`;
		case "EDIT_TEXT_BLOCK":
			return `${item.type}:${item.block}`;
		default:
			return null;
	}
}

function editOf(item: Change | Mutation): TextEdit {
	const { position, length, value } = item as TextEdit;

	return { position, length, value };
}

function client(): WebSocketClient {
	return Session.getWebSocketClient();
}
