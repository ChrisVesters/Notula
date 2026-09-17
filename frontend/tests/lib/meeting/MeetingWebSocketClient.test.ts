import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import CLIENT_ID from "$lib/common/ClientId";
import MeetingWebSocketClient from "$lib/meeting/MeetingWebSocketClient";
import type {
	Acknowledged,
	Change,
	Rejected
} from "$lib/meeting/change/ChangeTypes";

type Sent = {
	destination: string;
	body: string;
	headers: Record<string, string>;
};

const socket = vi.hoisted(() => {
	const callbacks = new Map<string, (message: { body: string }) => void>();
	const sent: Array<Sent> = [];

	return {
		callbacks,
		sent,
		subscribe(
			destination: string,
			callback: (message: { body: string }) => void
		) {
			callbacks.set(destination, callback);
		},
		unsubscribe(destination: string) {
			callbacks.delete(destination);
		},
		send(
			destination: string,
			body: string,
			headers: Record<string, string>
		) {
			sent.push({ destination, body, headers });
		},
		deliver(destination: string, payload: unknown) {
			const callback = callbacks.get(destination);
			if (callback === undefined) {
				throw new Error(`Not subscribed to ${destination}`);
			}

			callback({ body: JSON.stringify(payload) });
		},
		reset() {
			callbacks.clear();
			sent.length = 0;
		}
	};
});

vi.mock("$lib/auth/Session", () => ({
	default: { getWebSocketClient: () => socket }
}));

const MEETING_ID = 7;
const CHANGES = `/app/meetings/${MEETING_ID}/changes`;
const ACKS = "/user/queue/acks";
const REJECTIONS = "/user/queue/rejections";

const rename = (value: string): Change => ({
	type: "RENAME_MEETING",
	position: 0,
	length: 0,
	value
});

const onRejected = vi.fn();

const acknowledge = (id: string, revision: number) =>
	socket.deliver(ACKS, { id, revision } satisfies Acknowledged);

const changeIds = (): Array<string> =>
	socket.sent.map(sent => sent.headers["change-id"]);

const values = (): Array<string> =>
	socket.sent.map(sent => JSON.parse(sent.body).value);

describe("MeetingWebSocketClient", () => {
	beforeEach(() => {
		socket.reset();
		onRejected.mockClear();

		MeetingWebSocketClient.connect(MEETING_ID, {
			onLoad: vi.fn(),
			onEvent: vi.fn(),
			onRejected
		});
		socket.sent.length = 0;
	});

	afterEach(() => {
		MeetingWebSocketClient.disconnect();
	});

	it("sends a change with the client and change identifiers", () => {
		const id = MeetingWebSocketClient.send(rename("first"));

		expect(socket.sent).toEqual([
			{
				destination: CHANGES,
				body: JSON.stringify(rename("first")),
				headers: { "client-id": CLIENT_ID, "change-id": id }
			}
		]);
	});

	it("holds a change until the one before it is acknowledged", () => {
		const first = MeetingWebSocketClient.send(rename("first"));
		MeetingWebSocketClient.send(rename("second"));

		expect(changeIds()).toEqual([first]);

		acknowledge(first, 4);

		expect(values()).toEqual(["first", "second"]);
	});

	it("sends what is waiting in the order it was submitted", () => {
		const first = MeetingWebSocketClient.send(rename("first"));
		const second = MeetingWebSocketClient.send(rename("second"));
		MeetingWebSocketClient.send(rename("third"));

		acknowledge(first, 4);
		acknowledge(second, 5);

		expect(values()).toEqual(["first", "second", "third"]);
	});

	it("keeps holding on an acknowledgement for another change", () => {
		const first = MeetingWebSocketClient.send(rename("first"));
		MeetingWebSocketClient.send(rename("second"));

		acknowledge("1f0b3e4c-6d27-4a85-9c31-7b8e0d2f5a64", 4);

		expect(changeIds()).toEqual([first]);
	});

	it("drops what is waiting when a change is refused", () => {
		MeetingWebSocketClient.send(rename("first"));
		MeetingWebSocketClient.send(rename("second"));

		const rejected: Rejected = {
			id: changeIds()[0],
			retryable: false,
			reason: "It was not valid."
		};
		socket.deliver(REJECTIONS, rejected);

		expect(values()).toEqual(["first"]);
		expect(onRejected).toHaveBeenCalledWith(rejected);
	});

	it("sends again once a refusal has cleared the queue", () => {
		MeetingWebSocketClient.send(rename("first"));
		socket.deliver(REJECTIONS, {
			id: changeIds()[0],
			retryable: true,
			reason: "Try again."
		} satisfies Rejected);

		MeetingWebSocketClient.send(rename("second"));

		expect(values()).toEqual(["first", "second"]);
	});
});
