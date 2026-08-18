import Session from "$lib/auth/Session";
import type WebSocketClient from "$lib/common/WebSocketClient";

import type { BlockCreateAction, BlockDeleteAction } from "./BlockTypes";

export default class BlockWebSocketClient {
	private static readonly ENDPOINT = "/app/blocks";

	// TODO: remove duplicate code
	public static create(action: BlockCreateAction): void {
		const client: WebSocketClient = Session.getWebSocketClient();

		client.send(
			`${BlockWebSocketClient.ENDPOINT}/create`,
			JSON.stringify(action)
		);
	}

	public static delete(action: BlockDeleteAction): void {
		const client: WebSocketClient = Session.getWebSocketClient();

		client.send(
			`${BlockWebSocketClient.ENDPOINT}/delete`,
			JSON.stringify(action)
		);
	}
}
