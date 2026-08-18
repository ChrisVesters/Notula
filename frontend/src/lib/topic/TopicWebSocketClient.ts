import Session from "$lib/auth/Session";
import type WebSocketClient from "$lib/common/WebSocketClient";

import type {
	TopicCreateAction,
	TopicDeleteAction,
	TopicUpdateAction
} from "./TopicTypes";

export default class TopicWebSocketClient {
	private static readonly ENDPOINT = "/app/topics";

	public static create(topic: TopicCreateAction): void {
		TopicWebSocketClient.send("/create", topic);
	}

	public static update(action: TopicUpdateAction): void {
		TopicWebSocketClient.send("/update", action);
	}

	public static delete(action: TopicDeleteAction): void {
		TopicWebSocketClient.send("/delete", action);
	}

	private static send(suffix: string, payload: unknown): void {
		const client: WebSocketClient = Session.getWebSocketClient();

		client.send(
			`${TopicWebSocketClient.ENDPOINT}${suffix}`,
			JSON.stringify(payload)
		);
	}
}
