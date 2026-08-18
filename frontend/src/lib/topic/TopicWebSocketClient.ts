import Session from "$lib/auth/Session";
import type WebSocketClient from "$lib/common/WebSocketClient";

import type {
	TopicCreateAction,
	TopicDeleteAction,
	TopicUpdateAction
} from "./TopicTypes";

export default class TopicWebSocketClient {
	private static readonly ENDPOINT = "/app/topics";

	// TODO: remove duplicated code
	public static create(topic: TopicCreateAction): void {
		const client: WebSocketClient = Session.getWebSocketClient();

		client.send(
			`${TopicWebSocketClient.ENDPOINT}/create`,
			JSON.stringify(topic)
		);
	}

	public static update(action: TopicUpdateAction): void {
		const client: WebSocketClient = Session.getWebSocketClient();

		client.send(
			`${TopicWebSocketClient.ENDPOINT}/update`,
			JSON.stringify(action)
		);
	}

	public static delete(action: TopicDeleteAction): void {
		const client: WebSocketClient = Session.getWebSocketClient();

		client.send(
			`${TopicWebSocketClient.ENDPOINT}/delete`,
			JSON.stringify(action)
		);
	}
}
