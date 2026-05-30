import Session from "$lib/auth/Session";
import type WebSocketClient from "$lib/common/WebSocketClient";

import type { TextBlockUpdateContentAction } from "./TextBlockTypes";

export default class TextBlockWebSocketClient {
	public static updateContent(
		meetingId: number,
		topicId: number,
		blockId: number,
		action: TextBlockUpdateContentAction
	): void {
		const client: WebSocketClient = Session.getWebSocketClient();

		client.send(
			this.getDestination(meetingId, topicId, blockId),
			JSON.stringify(action)
		);
	}

	private static getDestination(
		meetingId: number,
		topicId: number,
		blockId: number
	) {
		return `/app/meetings/${meetingId}/topics/${topicId}/text-blocks/${blockId}`;
	}
}
