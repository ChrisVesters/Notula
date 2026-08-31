import Session from "$lib/auth/Session";

import CLIENT_ID from "./ClientId";
import type WebSocketClient from "./WebSocketClient";

export default class ActionWebSocketClient {
	public static send(destination: string, payload: unknown): void {
		const client: WebSocketClient = Session.getWebSocketClient();

		client.send(destination, JSON.stringify(payload), {
			"client-id": CLIENT_ID
		});
	}
}
