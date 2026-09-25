import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import CLIENT_ID from "$lib/common/ClientId";
import type { MeetingDetails } from "$lib/details/DetailTypes";
import MeetingWebSocketClient from "$lib/meeting/MeetingWebSocketClient";
import type {
	Acknowledged,
	Change,
	Rejected
} from "$lib/meeting/change/ChangeTypes";
import type { MeetingEvent } from "$lib/meeting/event/EventTypes";

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

describe("MeetingWebSocketClient stream", () => {
	const SNAPSHOT = `/app/meetings/${MEETING_ID}`;
	const EVENTS = `/topic/meetings/${MEETING_ID}`;
	const OTHER_CLIENT = "5b2e7c91-0d4a-4f63-8e15-3a9c6d0b7f42";

	const onLoad = vi.fn();
	const onEvent = vi.fn();

	beforeEach(() => {
		socket.reset();
		onLoad.mockClear();
		onEvent.mockClear();

		MeetingWebSocketClient.connect(MEETING_ID, {
			onLoad,
			onEvent,
			onRejected
		});
	});

	afterEach(() => {
		MeetingWebSocketClient.disconnect();
		vi.restoreAllMocks();
	});

	it("hands on an event that follows the snapshot", () => {
		const snapshot = { revision: 4 } as MeetingDetails;
		const event: MeetingEvent = {
			revision: 5,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 32 }
		};

		socket.deliver(SNAPSHOT, snapshot);
		socket.deliver(EVENTS, event);

		expect(onLoad).toHaveBeenCalledWith(snapshot);
		expect(onEvent).toHaveBeenCalledWith(event);
	});

	it("replays events that arrive before the snapshot onto it", () => {
		const snapshot = { revision: 4 } as MeetingDetails;
		const event: MeetingEvent = {
			revision: 5,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 32 }
		};

		socket.deliver(EVENTS, event);

		expect(onEvent).not.toHaveBeenCalled();

		socket.deliver(SNAPSHOT, snapshot);

		expect(onEvent).toHaveBeenCalledWith(event);
		expect(onLoad.mock.invocationCallOrder[0]).toBeLessThan(
			onEvent.mock.invocationCallOrder[0]
		);
	});

	it("drops events the snapshot already holds", () => {
		socket.deliver(SNAPSHOT, { revision: 5 } as MeetingDetails);
		socket.deliver(EVENTS, {
			revision: 4,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 31 }
		} satisfies MeetingEvent);
		socket.deliver(EVENTS, {
			revision: 5,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 32 }
		} satisfies MeetingEvent);

		expect(onEvent).not.toHaveBeenCalled();
	});

	it("hands on a second event at the revision it is streaming", () => {
		const first: MeetingEvent = {
			revision: 5,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 31 }
		};
		const second: MeetingEvent = {
			revision: 5,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 32 }
		};

		socket.deliver(SNAPSHOT, { revision: 4 } as MeetingDetails);
		socket.deliver(EVENTS, first);
		socket.deliver(EVENTS, second);

		expect(onEvent).toHaveBeenNthCalledWith(1, first);
		expect(onEvent).toHaveBeenNthCalledWith(2, second);
	});

	it("reloads the meeting when a revision is missing", () => {
		socket.deliver(SNAPSHOT, { revision: 4 } as MeetingDetails);
		const subscribe = vi.spyOn(socket, "subscribe");

		socket.deliver(EVENTS, {
			revision: 6,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 32 }
		} satisfies MeetingEvent);

		expect(onEvent).not.toHaveBeenCalled();
		expect(subscribe).toHaveBeenCalledTimes(1);
		expect(subscribe).toHaveBeenCalledWith(SNAPSHOT, expect.any(Function));
	});

	it("reloads once for a gap that spans several events", () => {
		socket.deliver(SNAPSHOT, { revision: 4 } as MeetingDetails);
		const subscribe = vi.spyOn(socket, "subscribe");

		socket.deliver(EVENTS, {
			revision: 6,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 31 }
		} satisfies MeetingEvent);
		socket.deliver(EVENTS, {
			revision: 7,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 32 }
		} satisfies MeetingEvent);

		expect(subscribe).toHaveBeenCalledTimes(1);
	});

	it("replays events that arrive during a reload onto the new snapshot", () => {
		const reloaded = { revision: 6 } as MeetingDetails;
		const event: MeetingEvent = {
			revision: 7,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 32 }
		};

		socket.deliver(SNAPSHOT, { revision: 4 } as MeetingDetails);
		socket.deliver(EVENTS, {
			revision: 6,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 31 }
		} satisfies MeetingEvent);
		socket.deliver(EVENTS, event);

		expect(onEvent).not.toHaveBeenCalled();

		socket.deliver(SNAPSHOT, reloaded);

		expect(onLoad).toHaveBeenLastCalledWith(reloaded);
		expect(onEvent).toHaveBeenCalledTimes(1);
		expect(onEvent).toHaveBeenCalledWith(event);
	});

	it("reloads on request once it holds a snapshot", () => {
		socket.deliver(SNAPSHOT, { revision: 4 } as MeetingDetails);
		const subscribe = vi.spyOn(socket, "subscribe");

		MeetingWebSocketClient.resync();

		expect(subscribe).toHaveBeenCalledWith(SNAPSHOT, expect.any(Function));
	});

	it("does not reload on request before the first snapshot", () => {
		const subscribe = vi.spyOn(socket, "subscribe");

		MeetingWebSocketClient.resync();

		expect(subscribe).not.toHaveBeenCalled();
	});

	it("drops the echo of its own text edit but keeps its revision", () => {
		const next: MeetingEvent = {
			revision: 6,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 32 }
		};
		const subscribe = vi.spyOn(socket, "subscribe");

		socket.deliver(SNAPSHOT, { revision: 4 } as MeetingDetails);
		socket.deliver(EVENTS, {
			revision: 5,
			origin: { userId: 1, clientId: CLIENT_ID },
			mutation: {
				type: "RENAME_TOPIC",
				topic: 32,
				position: 0,
				length: 0,
				value: "x"
			}
		} satisfies MeetingEvent);
		socket.deliver(EVENTS, next);

		expect(onEvent).toHaveBeenCalledTimes(1);
		expect(onEvent).toHaveBeenCalledWith(next);
		expect(subscribe).not.toHaveBeenCalled();
	});

	it("hands on the echo of its own structural change", () => {
		const echo: MeetingEvent = {
			revision: 5,
			origin: { userId: 1, clientId: CLIENT_ID },
			mutation: { type: "MOVE_TOPIC", topic: 32, rank: "V" }
		};

		socket.deliver(SNAPSHOT, { revision: 4 } as MeetingDetails);
		socket.deliver(EVENTS, echo);

		expect(onEvent).toHaveBeenCalledWith(echo);
	});

	it("hands on someone else's text edit", () => {
		const edit: MeetingEvent = {
			revision: 5,
			origin: { userId: 2, clientId: OTHER_CLIENT },
			mutation: {
				type: "EDIT_TEXT_BLOCK",
				block: 61,
				position: 0,
				length: 0,
				value: "x"
			}
		};

		socket.deliver(SNAPSHOT, { revision: 4 } as MeetingDetails);
		socket.deliver(EVENTS, edit);

		expect(onEvent).toHaveBeenCalledWith(edit);
	});
});
