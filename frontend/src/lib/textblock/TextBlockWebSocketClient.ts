import ActionWebSocketClient from "$lib/common/ActionWebSocketClient";

import type { TextBlockUpdateAction } from "./TextBlockTypes";

export default class TextBlockWebSocketClient {
	private static readonly ENDPOINT = "/app/text-blocks";

	public static updateContent(action: TextBlockUpdateAction): void {
		ActionWebSocketClient.send(
			`${TextBlockWebSocketClient.ENDPOINT}/update`,
			action
		);
	}
}
