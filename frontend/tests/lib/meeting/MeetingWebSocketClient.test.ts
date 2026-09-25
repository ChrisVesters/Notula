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
		socket.deliver(`/app/meetings/${MEETING_ID}`, {
			revision: 3
		} as MeetingDetails);
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
				body: JSON.stringify({ ...rename("first"), base: 3 }),
				headers: { "client-id": CLIENT_ID, "change-id": id }
			}
		]);
	});

	it("holds a change until the one before it is acknowledged and applied", () => {
		const first = MeetingWebSocketClient.send(rename("first"));
		MeetingWebSocketClient.send(rename("second"));

		expect(changeIds()).toEqual([first]);

		acknowledge(first, 4);

		expect(changeIds()).toEqual([first]);

		socket.deliver(`/topic/meetings/${MEETING_ID}`, {
			revision: 4,
			origin: { userId: 1, clientId: CLIENT_ID },
			mutation: {
				type: "RENAME_MEETING",
				position: 0,
				length: 0,
				value: "first"
			}
		} satisfies MeetingEvent);

		expect(values()).toEqual(["first", "second"]);
	});

	it("releases a change whose event arrives before its acknowledgement", () => {
		const first = MeetingWebSocketClient.send(rename("first"));
		MeetingWebSocketClient.send(rename("second"));

		socket.deliver(`/topic/meetings/${MEETING_ID}`, {
			revision: 4,
			origin: { userId: 1, clientId: CLIENT_ID },
			mutation: {
				type: "RENAME_MEETING",
				position: 0,
				length: 0,
				value: "first"
			}
		} satisfies MeetingEvent);

		expect(changeIds()).toEqual([first]);

		acknowledge(first, 4);

		expect(values()).toEqual(["first", "second"]);
	});

	it("sends what is waiting in the order it was submitted", () => {
		const first = MeetingWebSocketClient.send(rename("first"));
		const second = MeetingWebSocketClient.send({
			type: "DESCRIBE_MEETING",
			position: 0,
			length: 0,
			value: "second"
		});
		MeetingWebSocketClient.send(rename("third"));

		acknowledge(first, 4);
		socket.deliver(`/topic/meetings/${MEETING_ID}`, {
			revision: 4,
			origin: { userId: 1, clientId: CLIENT_ID },
			mutation: {
				type: "RENAME_MEETING",
				position: 0,
				length: 0,
				value: "first"
			}
		} satisfies MeetingEvent);
		acknowledge(second, 5);
		socket.deliver(`/topic/meetings/${MEETING_ID}`, {
			revision: 5,
			origin: { userId: 1, clientId: CLIENT_ID },
			mutation: {
				type: "DESCRIBE_MEETING",
				position: 0,
				length: 0,
				value: "second"
			}
		} satisfies MeetingEvent);

		expect(values()).toEqual(["first", "second", "third"]);
	});

	it("stamps a text change with the revision the page holds as it leaves", () => {
		const first = MeetingWebSocketClient.send(rename("first"));
		MeetingWebSocketClient.send(rename("second"));

		socket.deliver(`/topic/meetings/${MEETING_ID}`, {
			revision: 4,
			origin: {
				userId: 2,
				clientId: "5b2e7c91-0d4a-4f63-8e15-3a9c6d0b7f42"
			},
			mutation: { type: "REMOVE_TOPIC", topic: 32 }
		} satisfies MeetingEvent);
		acknowledge(first, 5);
		socket.deliver(`/topic/meetings/${MEETING_ID}`, {
			revision: 5,
			origin: { userId: 1, clientId: CLIENT_ID },
			mutation: {
				type: "RENAME_MEETING",
				position: 0,
				length: 0,
				value: "first"
			}
		} satisfies MeetingEvent);

		expect(socket.sent.map(sent => JSON.parse(sent.body).base)).toEqual([
			3, 5
		]);
	});

	it("sends a structural change without a base", () => {
		MeetingWebSocketClient.send({ type: "REMOVE_TOPIC", topic: 32 });

		expect(socket.sent.map(sent => JSON.parse(sent.body))).toEqual([
			{ type: "REMOVE_TOPIC", topic: 32 }
		]);
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

	it("sends again once a refusal has reloaded the meeting", () => {
		MeetingWebSocketClient.send(rename("first"));
		socket.deliver(REJECTIONS, {
			id: changeIds()[0],
			retryable: true,
			reason: "Try again."
		} satisfies Rejected);
		socket.deliver(`/app/meetings/${MEETING_ID}`, {
			revision: 3
		} as MeetingDetails);

		MeetingWebSocketClient.send(rename("second"));

		expect(values()).toEqual(["first", "second"]);
	});

	it("reloads the meeting when a change is refused", () => {
		MeetingWebSocketClient.send(rename("first"));
		const subscribe = vi.spyOn(socket, "subscribe");

		socket.deliver(REJECTIONS, {
			id: changeIds()[0],
			retryable: false,
			reason: "It was not valid."
		} satisfies Rejected);

		expect(subscribe).toHaveBeenCalledWith(
			`/app/meetings/${MEETING_ID}`,
			expect.any(Function)
		);
		vi.restoreAllMocks();
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

	it("sends nothing before the first snapshot", () => {
		MeetingWebSocketClient.send(rename("first"));

		expect(socket.sent).toEqual([]);
	});

	it("drops what is waiting when a snapshot replaces the page", () => {
		socket.deliver(SNAPSHOT, { revision: 4 } as MeetingDetails);
		const first = MeetingWebSocketClient.send(rename("first"));
		MeetingWebSocketClient.send(rename("second"));
		MeetingWebSocketClient.resync();

		socket.deliver(SNAPSHOT, { revision: 5 } as MeetingDetails);
		socket.deliver(ACKS, { id: first, revision: 5 } satisfies Acknowledged);

		expect(socket.sent.map(sent => JSON.parse(sent.body).value)).toEqual([
			"first"
		]);
	});

	it("applies the echo of its own text edit after a snapshot replaced the page", () => {
		const echo: MeetingEvent = {
			revision: 5,
			origin: { userId: 1, clientId: CLIENT_ID },
			mutation: {
				type: "RENAME_TOPIC",
				topic: 32,
				position: 0,
				length: 0,
				value: "x"
			}
		};

		socket.deliver(SNAPSHOT, { revision: 4 } as MeetingDetails);
		MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: 32,
			position: 0,
			length: 0,
			value: "x"
		});
		MeetingWebSocketClient.resync();
		socket.deliver(SNAPSHOT, { revision: 4 } as MeetingDetails);
		socket.deliver(EVENTS, echo);

		expect(onEvent).toHaveBeenCalledWith(echo);
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

	it("drops a repeat of the event it just applied", () => {
		const event: MeetingEvent = {
			revision: 5,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 32 }
		};

		socket.deliver(SNAPSHOT, { revision: 4 } as MeetingDetails);
		socket.deliver(EVENTS, event);
		socket.deliver(EVENTS, event);

		expect(onEvent).toHaveBeenCalledTimes(1);
	});

	it("asks for what was missed when a revision is missing", () => {
		socket.deliver(SNAPSHOT, { revision: 4 } as MeetingDetails);
		const subscribe = vi.spyOn(socket, "subscribe");

		socket.deliver(EVENTS, {
			revision: 6,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 32 }
		} satisfies MeetingEvent);

		expect(onEvent).not.toHaveBeenCalled();
		expect(subscribe).toHaveBeenCalledTimes(1);
		expect(subscribe).toHaveBeenCalledWith(
			`/app/meetings/${MEETING_ID}/events/4`,
			expect.any(Function)
		);
	});

	it("applies what was missed, then what arrived while asking", () => {
		const missed: MeetingEvent = {
			revision: 5,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 31 }
		};
		const gap: MeetingEvent = {
			revision: 6,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 32 }
		};
		const later: MeetingEvent = {
			revision: 7,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 33 }
		};

		socket.deliver(SNAPSHOT, { revision: 4 } as MeetingDetails);
		socket.deliver(EVENTS, gap);
		socket.deliver(EVENTS, later);

		expect(onEvent).not.toHaveBeenCalled();

		socket.deliver(`/app/meetings/${MEETING_ID}/events/4`, [missed, gap]);

		expect(onEvent).toHaveBeenCalledTimes(3);
		expect(onEvent).toHaveBeenNthCalledWith(1, missed);
		expect(onEvent).toHaveBeenNthCalledWith(2, gap);
		expect(onEvent).toHaveBeenNthCalledWith(3, later);
	});

	it("stops listening for what was missed once it has the answer", () => {
		socket.deliver(SNAPSHOT, { revision: 4 } as MeetingDetails);
		socket.deliver(EVENTS, {
			revision: 6,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 32 }
		} satisfies MeetingEvent);
		const unsubscribe = vi.spyOn(socket, "unsubscribe");

		socket.deliver(`/app/meetings/${MEETING_ID}/events/4`, []);

		expect(unsubscribe).toHaveBeenCalledWith(
			`/app/meetings/${MEETING_ID}/events/4`
		);
	});

	it("applies nothing twice from an answer that arrives after a reload", () => {
		const missed: MeetingEvent = {
			revision: 5,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 31 }
		};

		socket.deliver(SNAPSHOT, { revision: 4 } as MeetingDetails);
		socket.deliver(EVENTS, {
			revision: 6,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 32 }
		} satisfies MeetingEvent);
		const replay = socket.callbacks.get(
			`/app/meetings/${MEETING_ID}/events/4`
		);
		MeetingWebSocketClient.resync();
		socket.deliver(SNAPSHOT, { revision: 6 } as MeetingDetails);
		onEvent.mockClear();

		replay?.({ body: JSON.stringify([missed]) });

		expect(onEvent).not.toHaveBeenCalled();
	});

	it("hands on live events after a reload abandoned what it asked for", () => {
		const event: MeetingEvent = {
			revision: 7,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 33 }
		};

		socket.deliver(SNAPSHOT, { revision: 4 } as MeetingDetails);
		socket.deliver(EVENTS, {
			revision: 6,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 32 }
		} satisfies MeetingEvent);
		MeetingWebSocketClient.resync();
		socket.deliver(SNAPSHOT, { revision: 6 } as MeetingDetails);
		socket.deliver(EVENTS, event);

		expect(onEvent).toHaveBeenCalledWith(event);
	});

	it("asks once for a gap that spans several events", () => {
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

	it("keeps what it has not sent through a gap", () => {
		socket.deliver(SNAPSHOT, { revision: 4 } as MeetingDetails);
		const first = MeetingWebSocketClient.send({
			type: "REMOVE_TOPIC",
			topic: 31
		});
		MeetingWebSocketClient.send({ type: "REMOVE_TOPIC", topic: 32 });

		socket.deliver(EVENTS, {
			revision: 6,
			origin: { userId: 1, clientId: CLIENT_ID },
			mutation: { type: "REMOVE_TOPIC", topic: 31 }
		} satisfies MeetingEvent);
		socket.deliver(`/app/meetings/${MEETING_ID}/events/4`, [
			{
				revision: 5,
				origin: { userId: 2, clientId: OTHER_CLIENT },
				mutation: { type: "REMOVE_TOPIC", topic: 30 }
			},
			{
				revision: 6,
				origin: { userId: 1, clientId: CLIENT_ID },
				mutation: { type: "REMOVE_TOPIC", topic: 31 }
			}
		] satisfies Array<MeetingEvent>);
		socket.deliver(ACKS, { id: first, revision: 6 } satisfies Acknowledged);

		expect(socket.sent.map(sent => JSON.parse(sent.body))).toEqual([
			{ type: "REMOVE_TOPIC", topic: 31 },
			{ type: "REMOVE_TOPIC", topic: 32 }
		]);
	});

	it("replays events that arrive during a reload onto the new snapshot", () => {
		const reloaded = { revision: 6 } as MeetingDetails;
		const event: MeetingEvent = {
			revision: 7,
			origin: { userId: 1, clientId: OTHER_CLIENT },
			mutation: { type: "REMOVE_TOPIC", topic: 32 }
		};

		socket.deliver(SNAPSHOT, { revision: 4 } as MeetingDetails);
		MeetingWebSocketClient.resync();
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
		MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: 32,
			position: 0,
			length: 0,
			value: "x"
		});
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
		MeetingWebSocketClient.send({
			type: "MOVE_TOPIC",
			topic: 32,
			afterId: null
		});
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

describe("MeetingWebSocketClient rebase", () => {
	const SNAPSHOT = `/app/meetings/${MEETING_ID}`;
	const EVENTS = `/topic/meetings/${MEETING_ID}`;
	const OTHER_CLIENT = "5b2e7c91-0d4a-4f63-8e15-3a9c6d0b7f42";

	const onEvent = vi.fn();

	beforeEach(() => {
		socket.reset();
		onEvent.mockClear();

		MeetingWebSocketClient.connect(MEETING_ID, {
			onLoad: vi.fn(),
			onEvent,
			onRejected
		});
		socket.deliver(SNAPSHOT, { revision: 4 } as MeetingDetails);
	});

	afterEach(() => {
		MeetingWebSocketClient.disconnect();
	});

	it("moves a remote edit over the one in flight", () => {
		MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: 32,
			position: 0,
			length: 0,
			value: "Q3 "
		});

		socket.deliver(EVENTS, {
			revision: 5,
			origin: { userId: 2, clientId: OTHER_CLIENT },
			mutation: {
				type: "RENAME_TOPIC",
				topic: 32,
				position: 8,
				length: 0,
				value: " plan"
			}
		} satisfies MeetingEvent);

		expect(onEvent).toHaveBeenCalledWith({
			revision: 5,
			origin: { userId: 2, clientId: OTHER_CLIENT },
			mutation: {
				type: "RENAME_TOPIC",
				topic: 32,
				position: 11,
				length: 0,
				value: " plan"
			}
		});
	});

	it("moves what is waiting over a remote edit", () => {
		const first = MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: 32,
			position: 0,
			length: 0,
			value: "Q3 "
		});
		MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: 32,
			position: 11,
			length: 0,
			value: "!"
		});

		socket.deliver(EVENTS, {
			revision: 5,
			origin: { userId: 2, clientId: OTHER_CLIENT },
			mutation: {
				type: "RENAME_TOPIC",
				topic: 32,
				position: 0,
				length: 0,
				value: "New "
			}
		} satisfies MeetingEvent);
		socket.deliver(ACKS, { id: first, revision: 6 } satisfies Acknowledged);
		socket.deliver(EVENTS, {
			revision: 6,
			origin: { userId: 1, clientId: CLIENT_ID },
			mutation: {
				type: "RENAME_TOPIC",
				topic: 32,
				position: 4,
				length: 0,
				value: "Q3 "
			}
		} satisfies MeetingEvent);

		expect(JSON.parse(socket.sent[1].body)).toEqual({
			type: "RENAME_TOPIC",
			topic: 32,
			position: 15,
			length: 0,
			value: "!",
			base: 6
		});
	});

	it("leaves a remote edit to other text where it is", () => {
		MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: 32,
			position: 0,
			length: 0,
			value: "Q3 "
		});

		socket.deliver(EVENTS, {
			revision: 5,
			origin: { userId: 2, clientId: OTHER_CLIENT },
			mutation: {
				type: "DESCRIBE_TOPIC",
				topic: 32,
				position: 8,
				length: 0,
				value: " plan"
			}
		} satisfies MeetingEvent);

		expect(onEvent).toHaveBeenCalledWith({
			revision: 5,
			origin: { userId: 2, clientId: OTHER_CLIENT },
			mutation: {
				type: "DESCRIBE_TOPIC",
				topic: 32,
				position: 8,
				length: 0,
				value: " plan"
			}
		});
	});

	it("leaves a remote edit to the same field of another topic where it is", () => {
		MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: 32,
			position: 0,
			length: 0,
			value: "Q3 "
		});

		socket.deliver(EVENTS, {
			revision: 5,
			origin: { userId: 2, clientId: OTHER_CLIENT },
			mutation: {
				type: "RENAME_TOPIC",
				topic: 33,
				position: 8,
				length: 0,
				value: " plan"
			}
		} satisfies MeetingEvent);

		expect(onEvent).toHaveBeenCalledWith({
			revision: 5,
			origin: { userId: 2, clientId: OTHER_CLIENT },
			mutation: {
				type: "RENAME_TOPIC",
				topic: 33,
				position: 8,
				length: 0,
				value: " plan"
			}
		});
	});

	it("stops moving remote edits over its own once the echo is in", () => {
		MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: 32,
			position: 0,
			length: 0,
			value: "Q3 "
		});
		socket.deliver(EVENTS, {
			revision: 5,
			origin: { userId: 1, clientId: CLIENT_ID },
			mutation: {
				type: "RENAME_TOPIC",
				topic: 32,
				position: 0,
				length: 0,
				value: "Q3 "
			}
		} satisfies MeetingEvent);

		socket.deliver(EVENTS, {
			revision: 6,
			origin: { userId: 2, clientId: OTHER_CLIENT },
			mutation: {
				type: "RENAME_TOPIC",
				topic: 32,
				position: 11,
				length: 0,
				value: " plan"
			}
		} satisfies MeetingEvent);

		expect(onEvent).toHaveBeenCalledWith({
			revision: 6,
			origin: { userId: 2, clientId: OTHER_CLIENT },
			mutation: {
				type: "RENAME_TOPIC",
				topic: 32,
				position: 11,
				length: 0,
				value: " plan"
			}
		});
	});
});

describe("MeetingWebSocketClient compose", () => {
	beforeEach(() => {
		socket.reset();

		MeetingWebSocketClient.connect(MEETING_ID, {
			onLoad: vi.fn(),
			onEvent: vi.fn(),
			onRejected
		});
		socket.deliver(`/app/meetings/${MEETING_ID}`, {
			revision: 3
		} as MeetingDetails);
	});

	afterEach(() => {
		MeetingWebSocketClient.disconnect();
	});

	it("sends keystrokes typed while a change is in flight as one change", () => {
		const first = MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: 32,
			position: 8,
			length: 0,
			value: " "
		});
		const second = MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: 32,
			position: 9,
			length: 0,
			value: "p"
		});
		const third = MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: 32,
			position: 10,
			length: 0,
			value: "l"
		});
		MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: 32,
			position: 11,
			length: 0,
			value: "an"
		});

		socket.deliver(ACKS, { id: first, revision: 4 } satisfies Acknowledged);
		socket.deliver(`/topic/meetings/${MEETING_ID}`, {
			revision: 4,
			origin: { userId: 1, clientId: CLIENT_ID },
			mutation: {
				type: "RENAME_TOPIC",
				topic: 32,
				position: 8,
				length: 0,
				value: " "
			}
		} satisfies MeetingEvent);

		expect(third).toBe(second);
		expect(socket.sent.map(sent => JSON.parse(sent.body))).toEqual([
			{
				type: "RENAME_TOPIC",
				topic: 32,
				position: 8,
				length: 0,
				value: " ",
				base: 3
			},
			{
				type: "RENAME_TOPIC",
				topic: 32,
				position: 9,
				length: 0,
				value: "plan",
				base: 4
			}
		]);
	});

	it("keeps an edit to other text apart", () => {
		MeetingWebSocketClient.send(rename("first"));
		const second = MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: 32,
			position: 0,
			length: 0,
			value: "a"
		});
		const third = MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: 33,
			position: 1,
			length: 0,
			value: "b"
		});

		expect(third).not.toBe(second);
	});

	it("keeps an edit that does not touch the one waiting apart", () => {
		MeetingWebSocketClient.send(rename("first"));
		const second = MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: 32,
			position: 0,
			length: 0,
			value: "a"
		});
		const third = MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: 32,
			position: 5,
			length: 0,
			value: "b"
		});

		expect(third).not.toBe(second);
	});

	it("does not join the change in flight", () => {
		const first = MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: 32,
			position: 0,
			length: 0,
			value: "a"
		});
		const second = MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: 32,
			position: 1,
			length: 0,
			value: "b"
		});

		expect(second).not.toBe(first);
		expect(socket.sent).toHaveLength(1);
	});

	it("only joins the last change waiting", () => {
		MeetingWebSocketClient.send(rename("first"));
		const second = MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: 32,
			position: 0,
			length: 0,
			value: "a"
		});
		MeetingWebSocketClient.send({ type: "REMOVE_TOPIC", topic: 40 });
		const fourth = MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: 32,
			position: 1,
			length: 0,
			value: "b"
		});

		expect(fourth).not.toBe(second);
	});
});
