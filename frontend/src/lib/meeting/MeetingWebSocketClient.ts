import Session from "$lib/auth/Session";
import CLIENT_ID from "$lib/common/ClientId";
import type WebSocketClient from "$lib/common/WebSocketClient";
import type { MeetingDetails } from "$lib/details/DetailTypes";

import type {
	Acknowledged,
	Change,
	Rejected,
	Submission
} from "./change/ChangeTypes";
import type { MeetingEvent } from "./event/EventTypes";

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

	static #inFlight: Submission | null = null;
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
			handler.onEvent(JSON.parse(message.body))
		);
		MeetingWebSocketClient.#load(id, handler);
	}

	public static resync(): void {
		const id = MeetingWebSocketClient.#meetingId;
		const handler = MeetingWebSocketClient.#handler;
		if (id === null || handler === null) {
			throw new Error("No meeting to reload");
		}

		client().unsubscribe(`/app/meetings/${id}`);
		MeetingWebSocketClient.#load(id, handler);
	}

	public static disconnect(): void {
		const id = MeetingWebSocketClient.#meetingId;
		MeetingWebSocketClient.#meetingId = null;
		MeetingWebSocketClient.#handler = null;
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

	static #acknowledge(acknowledged: Acknowledged): void {
		if (MeetingWebSocketClient.#inFlight?.id !== acknowledged.id) {
			return;
		}

		MeetingWebSocketClient.#inFlight = null;
		MeetingWebSocketClient.#submit();
	}

	static #submit(): void {
		const meetingId = MeetingWebSocketClient.#meetingId;
		if (meetingId === null || MeetingWebSocketClient.#inFlight !== null) {
			return;
		}

		const next = MeetingWebSocketClient.#queued.shift();
		if (next === undefined) {
			return;
		}

		MeetingWebSocketClient.#inFlight = next;
		client().send(
			`/app/meetings/${meetingId}/changes`,
			JSON.stringify(next.change),
			{ "client-id": CLIENT_ID, "change-id": next.id }
		);
	}

	static #clear(): void {
		MeetingWebSocketClient.#inFlight = null;
		MeetingWebSocketClient.#queued = [];
	}

	static #load(id: number, handler: MeetingEventHandler): void {
		client().subscribe(`/app/meetings/${id}`, message =>
			handler.onLoad(JSON.parse(message.body))
		);
	}
}

function client(): WebSocketClient {
	return Session.getWebSocketClient();
}
