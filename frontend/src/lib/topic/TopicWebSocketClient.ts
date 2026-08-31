import ActionWebSocketClient from "$lib/common/ActionWebSocketClient";

import type {
	TopicCreateAction,
	TopicDeleteAction,
	TopicMoveAction,
	TopicUpdateAction
} from "./TopicTypes";

export default class TopicWebSocketClient {
	private static readonly ENDPOINT = "/app/topics";

	public static create(topic: TopicCreateAction): void {
		TopicWebSocketClient.send("/create", topic);
	}

	public static move(action: TopicMoveAction): void {
		TopicWebSocketClient.send("/move", action);
	}

	public static update(action: TopicUpdateAction): void {
		TopicWebSocketClient.send("/update", action);
	}

	public static delete(action: TopicDeleteAction): void {
		TopicWebSocketClient.send("/delete", action);
	}

	private static send(suffix: string, payload: unknown): void {
		ActionWebSocketClient.send(
			`${TopicWebSocketClient.ENDPOINT}${suffix}`,
			payload
		);
	}
}
