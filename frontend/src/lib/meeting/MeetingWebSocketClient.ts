import Session from "$lib/auth/Session";
import WebSocketClient from "$lib/common/WebSocketClient";
import type {
	MeetingActionResponse,
	MeetingDetails,
	MeetingUpdateNameAction
} from "./MeetingTypes";

export type MeetingEventHandler = {
	onLoad: (data: MeetingDetails) => void;
	onError: (message: string) => void;
	onEvent: (event: MeetingActionResponse) => void;
};

export default class MeetingWebSocketClient {
	public static connect(id: number, handler: MeetingEventHandler): void {
		const client: WebSocketClient = Session.getWebSocketClient();

		client.subscribe("/user/queue/errors", message =>
			handler.onError(message.body)
		);
		client.subscribe(`/topic/meetings/${id}`, message =>
			handler.onEvent(JSON.parse(message.body))
		);
		client.subscribe(`/app/meetings/${id}`, message =>
			handler.onLoad(JSON.parse(message.body))
		);
	}

	public static disconnect(id: number): void {
		const client: WebSocketClient = Session.getWebSocketClient();

		client.unsubscribe(`/app/meetings/${id}`);
		client.unsubscribe(`/topic/meetings/${id}`);
		client.unsubscribe("/user/queue/errors");
	}

	public static updateName(
		id: number,
		action: MeetingUpdateNameAction
	): void {
		const client: WebSocketClient = Session.getWebSocketClient();

		client.send(`/app/meetings/${id}`, JSON.stringify(action));
	}
}
