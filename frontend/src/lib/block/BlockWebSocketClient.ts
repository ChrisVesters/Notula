import Session from "$lib/auth/Session";
import type WebSocketClient from "$lib/common/WebSocketClient";

import type { BlockCreateAction, BlockDeleteAction } from "./BlockTypes";

export default class BlockWebSocketClient {
	private static readonly ENDPOINT = "/app/blocks";

	public static create(action: BlockCreateAction): void {
		BlockWebSocketClient.send("/create", action);
	}

	public static delete(action: BlockDeleteAction): void {
		BlockWebSocketClient.send("/delete", action);
	}

	private static send(suffix: string, payload: unknown): void {
		const client: WebSocketClient = Session.getWebSocketClient();

		client.send(
			`${BlockWebSocketClient.ENDPOINT}${suffix}`,
			JSON.stringify(payload)
		);
	}
}
