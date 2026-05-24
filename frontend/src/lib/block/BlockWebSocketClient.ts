import Session from "$lib/auth/Session";
import type WebSocketClient from "$lib/common/WebSocketClient";

import type { BlockCreateRequest } from "./BlockTypes";

export default class BlockWebSocketClient {
	public static create(
		meetingId: number,
		topicId: number,
		block: BlockCreateRequest
	): void {
		const client: WebSocketClient = Session.getWebSocketClient();

		client.send(
			this.getDestination(meetingId, topicId),
			JSON.stringify(block)
		);
	}

	private static getDestination(
		meetingId: number,
		topicId: number,
		blockId?: number
	) {
		const suffix = blockId ? `/${blockId}` : "";
		return `/app/meetings/${meetingId}/topics/${topicId}/blocks${suffix}`;
	}
}
