import ActionWebSocketClient from "$lib/common/ActionWebSocketClient";

import type {
	BlockCreateAction,
	BlockDeleteAction,
	BlockMoveAction
} from "./BlockTypes";

export default class BlockWebSocketClient {
	private static readonly ENDPOINT = "/app/blocks";

	public static create(action: BlockCreateAction): void {
		BlockWebSocketClient.send("/create", action);
	}

	public static move(action: BlockMoveAction): void {
		BlockWebSocketClient.send("/move", action);
	}

	public static delete(action: BlockDeleteAction): void {
		BlockWebSocketClient.send("/delete", action);
	}

	private static send(suffix: string, payload: unknown): void {
		ActionWebSocketClient.send(
			`${BlockWebSocketClient.ENDPOINT}${suffix}`,
			payload
		);
	}
}
