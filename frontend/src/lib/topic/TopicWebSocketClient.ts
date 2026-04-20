import Session from "$lib/auth/Session";
import type WebSocketClient from "$lib/common/WebSocketClient";

import type { TopicCreateRequest } from "./TopicTypes";

export default class TopicWebSocketClient {
	public static create(meetingId: number, topic: TopicCreateRequest): void {
		const client: WebSocketClient = Session.getWebSocketClient();

		client.send(`/app/meetings/${meetingId}/topics`, JSON.stringify(topic));
	}
}
