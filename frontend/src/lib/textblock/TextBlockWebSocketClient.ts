import Session from "$lib/auth/Session";
import type WebSocketClient from "$lib/common/WebSocketClient";
import type { TextBlockUpdateAction } from "./TextBlockTypes";

export default class TextBlockWebSocketClient {
	private static readonly ENDPOINT = "/app/text-blocks";

	public static updateContent(action: TextBlockUpdateAction): void {
		const client: WebSocketClient = Session.getWebSocketClient();

		client.send(
			`${TextBlockWebSocketClient.ENDPOINT}/update`,
			JSON.stringify(action)
		);
	}
}
