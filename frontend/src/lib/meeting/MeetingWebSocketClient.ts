import Session from "$lib/auth/Session";
import CLIENT_ID from "$lib/common/ClientId";
import { isOwnEvent } from "$lib/common/EventTypes";
import type WebSocketClient from "$lib/common/WebSocketClient";
import type { MeetingDetails } from "$lib/details/DetailTypes";

import type {
	Acknowledged,
	Change,
	Rejected,
	Submission
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
	static #queued: Array<Submission> = [];

	public static connect(id: number, handler: MeetingEventHandler): void {
		MeetingWebSocketClient.#meetingId = id;
		MeetingWebSocketClient.#handler = handler;

		client().subscribe(MeetingWebSocketClient.#ACKS, message =>
			MeetingWebSocketClient.#acknowledge(JSON.parse(message.body))
		);
		client().subscribe(MeetingWebSocketClient.#REJECTIONS, message => {
			const rejected: Rejected = JSON.parse(message.body);

			MeetingWebSocketClient.#clear();
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

		const id: string = crypto.randomUUID();

		MeetingWebSocketClient.#queued.push({ id, change });
		MeetingWebSocketClient.#submit();

		return id;
	}

	static #onLoad(data: MeetingDetails): void {
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

		if (!isOwnEvent(event.origin) || !isText(event.mutation)) {
			MeetingWebSocketClient.#handler?.onEvent(event);
		}

		MeetingWebSocketClient.#release();
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
		const body = isText(next.change)
			? { ...next.change, base: revision }
			: next.change;

		MeetingWebSocketClient.#inFlight = next;
		client().send(
			`/app/meetings/${meetingId}/changes`,
			JSON.stringify(body),
			{ "client-id": CLIENT_ID, "change-id": next.id }
		);
	}

	static #clear(): void {
		MeetingWebSocketClient.#inFlight = null;
		MeetingWebSocketClient.#acknowledged = null;
		MeetingWebSocketClient.#queued = [];
	}

	static #load(id: number): void {
		client().subscribe(`/app/meetings/${id}`, message =>
			MeetingWebSocketClient.#onLoad(JSON.parse(message.body))
		);
	}
}

function isText(item: Change | Mutation): boolean {
	switch (item.type) {
		case "RENAME_MEETING":
		case "DESCRIBE_MEETING":
		case "RENAME_TOPIC":
		case "DESCRIBE_TOPIC":
		case "EDIT_TEXT_BLOCK":
			return true;
		default:
			return false;
	}
}

function client(): WebSocketClient {
	return Session.getWebSocketClient();
}
