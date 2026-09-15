import Session from "$lib/auth/Session";
import CLIENT_ID from "$lib/common/ClientId";
import type WebSocketClient from "$lib/common/WebSocketClient";
import type { MeetingDetails } from "$lib/details/DetailTypes";

import type { Change, Rejected } from "./change/ChangeTypes";
import type { MeetingEvent } from "./event/EventTypes";

export type MeetingEventHandler = {
	onLoad: (data: MeetingDetails) => void;
	onEvent: (event: MeetingEvent) => void;
	onRejected: (rejected: Rejected) => void;
};

export default class MeetingWebSocketClient {
	static readonly #REJECTIONS = "/user/queue/rejections";

	static #meetingId: number | null = null;

	public static connect(id: number, handler: MeetingEventHandler): void {
		MeetingWebSocketClient.#meetingId = id;

		client().subscribe(MeetingWebSocketClient.#REJECTIONS, message =>
			handler.onRejected(JSON.parse(message.body))
		);
		client().subscribe(`/topic/meetings/${id}`, message =>
			handler.onEvent(JSON.parse(message.body))
		);
		client().subscribe(`/app/meetings/${id}`, message =>
			handler.onLoad(JSON.parse(message.body))
		);
	}

	public static disconnect(): void {
		const id = MeetingWebSocketClient.#meetingId;
		MeetingWebSocketClient.#meetingId = null;

		client().unsubscribe(`/app/meetings/${id}`);
		client().unsubscribe(`/topic/meetings/${id}`);
		client().unsubscribe(MeetingWebSocketClient.#REJECTIONS);
	}

	public static send(change: Change): string {
		const meetingId = MeetingWebSocketClient.#meetingId;
		if (meetingId === null) {
			throw new Error("No meeting to change");
		}

		const id: string = crypto.randomUUID();

		client().send(
			`/app/meetings/${meetingId}/changes`,
			JSON.stringify(change),
			{ "client-id": CLIENT_ID, "change-id": id }
		);

		return id;
	}
}

function client(): WebSocketClient {
	return Session.getWebSocketClient();
}
