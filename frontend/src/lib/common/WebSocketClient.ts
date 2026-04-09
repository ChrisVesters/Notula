import {
	Client,
	type IMessage,
	type IPublishParams,
	type messageCallbackType,
	type StompSubscription
} from "@stomp/stompjs";

type Registration = {
	callback: (message: IMessage) => void;
	subscription?: StompSubscription;
};

// TODO: store like authentication/principal?
export default class WebSocketClient {
	private readonly client: Client;
	private readonly sendQueue: Array<IPublishParams> = [];
	// Only 1 subscription per destination!?
	private readonly subscriptions: Map<string, Registration> = new Map();

	constructor(accessToken: string) {
		// TODO: What happens if token expires?
		this.client = new Client({
			brokerURL: import.meta.env.VITE_WS_URL,
			connectHeaders: {
				Authorization: `Bearer ${accessToken}`
			},
			heartbeatIncoming: 10000,
			heartbeatOutgoing: 10000
			// reconnectDelay: 5000,
		});

		this.client.onConnect = () => {
			console.log("Connected to STOMP broker");

			this.flushQueue();
			this.subscriptions.forEach((registration, destination) => {
				registration.subscription = this.client.subscribe(
					destination,
					registration.callback
				);
			});
		};

		this.client.onDisconnect = () => {
			console.log("Disconnected from STOMP broker");

			this.subscriptions.forEach(registration => {
				registration.subscription == undefined;
			});
		};

		this.client.onStompError = frame => {
			console.error("Broker error:", frame.headers["message"]);
		};

		this.client.activate();
	}

	public disconnect(): void {
		console.log("Disconnecting from STOMP broker");
		this.client.deactivate();
	}

	public send(destination: string, body: string, headers = {}): void {
		const msg = {
			destination,
			body
			// headers: { ...headers, "message-id": crypto.randomUUID() }
		};

		if (this.client.connected) {
			this.client.publish(msg);
		} else {
			this.sendQueue.push(msg);
			console.log("Message queued:", msg);
		}
	}

	private flushQueue() {
		while (this.sendQueue.length > 0 && this.client.connected) {
			const msg = this.sendQueue.shift();
			// TODO: bit nasty!
			msg && this.client.publish(msg);
		}
	}

	public subscribe(destination: string, callback: messageCallbackType) {
		if (this.subscriptions.has(destination)) {
			console.warn("Already subscribed to", destination);
			return;
		}

		const registration: Registration = {
			callback
		};

		this.subscriptions.set(destination, registration);
		if (this.client.connected) {
			registration.subscription = this.client.subscribe(
				destination,
				callback,
				{
					ack: "client-individual"
				}
			);
		}
	}

	public unsubscribe(destination: string) {
		this.subscriptions.get(destination)?.subscription?.unsubscribe();
		this.subscriptions.delete(destination);
	}
}
