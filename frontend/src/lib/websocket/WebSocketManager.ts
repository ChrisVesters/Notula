import {
	Client,
	type messageCallbackType,
	type StompSubscription
} from "@stomp/stompjs";

import Session from "$lib/auth/Session";

// TODO: subscribe inside onConnect to handle reconnect


/**
 *  Store messages in queue
 * 
let sendQueue = [];

function sendMessage(destination, body) {
  if (client.connected) {
    client.publish({ destination, body });
  } else {
    // store in queue
    sendQueue.push({ destination, body });
  }
}

client.onConnect = () => {
  // Re-subscribe to topics here
  // Then flush queued messages
  while (sendQueue.length > 0) {
    const msg = sendQueue.shift();
    client.publish(msg);
  }
};


// TODO: message id?


  const trySend = () => {
    if (!client.connected) {
      // Queue if disconnected
      sendQueue.push(msg);
      return;
    }

    try {
      client.publish(msg);
    } catch (err) {
      if (attempts < maxRetries) {
        attempts++;
        setTimeout(trySend, delay * attempts); // exponential backoff
      } else {
        console.error('Failed to send message after retries:', msg);
      }
    }
  };

 * 
 * 
 */


/**
 * 
import { Client } from '@stomp/stompjs';

export class ReliableStompClient {
  constructor(brokerURL, reconnectDelay = 5000) {
    this.client = new Client({
      brokerURL,
      reconnectDelay,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    });

    this.sendQueue = [];
    this.subscriptions = new Map(); // store app subscriptions

    this.client.onConnect = () => {
      console.log('Connected to STOMP broker');

      // flush queued messages
      this.flushQueue();

      // re-subscribe all application subscriptions
      this.subscriptions.forEach((callback, destination) => {
        this.client.subscribe(destination, callback);
      });
    };

    this.client.onStompError = (frame) => {
      console.error('Broker error:', frame.headers['message']);
    };
  }

  activate() {
    this.client.activate();
  }

  deactivate() {
    this.client.deactivate();
  }

  // ----------------------
  // Send message safely
  // ----------------------
  send(destination, body, headers = {}) {
    const msg = { destination, body, headers: { ...headers, 'message-id': crypto.randomUUID() } };

    if (this.client.connected) {
      this.client.publish(msg);
    } else {
      this.sendQueue.push(msg);
      console.log('Message queued:', msg);
    }
  }

  flushQueue() {
    while (this.sendQueue.length > 0 && this.client.connected) {
      const msg = this.sendQueue.shift();
      this.client.publish(msg);
    }
  }

  // ----------------------
  // Subscribe dynamically
  // ----------------------
  subscribe(destination, callback) {
    this.subscriptions.set(destination, callback);

    if (this.client.connected) {
      this.client.subscribe(destination, callback);
    }
  }

  unsubscribe(destination) {
    this.subscriptions.delete(destination);
    // Note: StompJS does not automatically expose unsubscribe by destination,
    // you need to keep the subscription object if you want to unsubscribe:
    // const sub = this.client.subscribe(...);
    // sub.unsubscribe();
  }
}
 * 
 */


export default class WebSocketManager {
	static #CLIENT: Client | null = null;
	static #CONNECTION: Promise<void> = Promise.resolve();

	public static connect(): void {
		if (WebSocketManager.#CLIENT !== null) {
			return;
		}

		WebSocketManager.#CLIENT = new Client({
			brokerURL: import.meta.env.VITE_WS_URL,
			connectHeaders: {
				Authorization: "Bearer " + Session.getAccessToken()
			}
			// reconnectDelay: 5000,
			// heartbeatIncoming: 4000,
			// heartbeatOutgoing: 4000,
		});

		// TODO: see how it behaves when connection drops, etc..
		// TODO: Need to make this more robust.
		WebSocketManager.#CONNECTION = new Promise((resolve, reject) => {
			if (WebSocketManager.#CLIENT === null) {
				reject();
				return;
			}

			WebSocketManager.#CLIENT.onConnect = () => resolve();
			WebSocketManager.#CLIENT.onStompError = frame => reject(frame);
		});

		WebSocketManager.#CLIENT.activate();
	}

	public static async send(destination: string, body: string): Promise<void> {
		if (WebSocketManager.#CLIENT === null) {
			throw Error("WebSocket not connected!");
		}

		await WebSocketManager.#CONNECTION;

		WebSocketManager.#CLIENT.publish({
			destination: destination,
			body: body
		});
	}

	// TODO: abstract subscription!? and types
	public static async subscribe(
		destination: string,
		callback: messageCallbackType
	): Promise<StompSubscription> {
		if (WebSocketManager.#CLIENT === null) {
			throw Error("WebSocket not connected!");
		}

		await WebSocketManager.#CONNECTION;

		console.log("subscribe to ", destination);
		return WebSocketManager.#CLIENT.subscribe(destination, callback, {
			ack: "client-individual"
		});
	}

	public static async disconnect(): Promise<void> {
		if (WebSocketManager.#CLIENT === null) {
			return;
		}

		await WebSocketManager.#CLIENT.deactivate();
		WebSocketManager.#CLIENT = null;
	}
}
