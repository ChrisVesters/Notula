import Session from "$lib/auth/Session";
import type WebSocketClient from "$lib/common/WebSocketClient";

import type {
	TopicAction,
	TopicCreateRequest
} from "./TopicTypes";

export default class TopicWebSocketClient {
	public static create(meetingId: number, topic: TopicCreateRequest): void {
		const client: WebSocketClient = Session.getWebSocketClient();

		client.send(this.getDestination(meetingId), JSON.stringify(topic));
	}

	public static update(
		meetingId: number,
		topicId: number,
		action: TopicAction
	): void {
		const client: WebSocketClient = Session.getWebSocketClient();

		client.send(
			this.getDestination(meetingId, topicId),
			JSON.stringify(action)
		);
	}

	public static delete(meetingId: number, topicId: number): void {
		const client: WebSocketClient = Session.getWebSocketClient();

		client.send(this.getDestination(meetingId, topicId) + "/delete", "");
	}

	private static getDestination(meetingId: number, topicId?: number) {
		const suffix = topicId ? `/${topicId}` : "";
		return `/app/meetings/${meetingId}/topics${suffix}`;
	}
}
