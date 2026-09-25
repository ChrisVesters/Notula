import { describe, expect, it } from "vitest";

import { applied, moved, rebased } from "$lib/editor/TextEdit";
import type { TextEdit } from "$lib/meeting/change/ChangeTypes";

describe("applied", () => {
	it("insert", () => {
		const result = applied("Hell world", {
			position: 4,
			length: 0,
			value: "o"
		});

		expect(result).toBe("Hello world");
	});

	it("replace", () => {
		const result = applied("Hello world", {
			position: 6,
			length: 5,
			value: "there"
		});

		expect(result).toBe("Hello there");
	});

	it("delete", () => {
		const result = applied("Hello world", {
			position: 5,
			length: 6,
			value: ""
		});

		expect(result).toBe("Hello");
	});

	it("append", () => {
		const result = applied("Hello", {
			position: 5,
			length: 0,
			value: "!"
		});

		expect(result).toBe("Hello!");
	});
});

describe("moved", () => {
	it("unchanged", () => {
		expect(moved("Hello", "Hello", 3)).toBe(3);
	});

	it("before", () => {
		expect(moved("Hello world", "Hello there", 4)).toBe(4);
	});

	it("afterInsert", () => {
		expect(moved("Hell world", "Hello world", 9)).toBe(10);
	});

	it("afterDelete", () => {
		expect(moved("Hello there world", "Hello world", 16)).toBe(10);
	});

	it("inside", () => {
		expect(moved("Hello world", "Hello there", 8)).toBe(11);
	});

	it("atChange", () => {
		expect(moved("Hell world", "Hello world", 4)).toBe(4);
	});
});

describe("rebased", () => {
	const TEXT = "abcd";

	it.each([
		[
			{ position: 0, length: 1, value: "x" },
			{ position: 2, length: 1, value: "p" },
			{ position: 0, length: 1, value: "x" }
		],
		[
			{ position: 0, length: 1, value: "x" },
			{ position: 1, length: 1, value: "p" },
			{ position: 0, length: 1, value: "x" }
		],
		[
			{ position: 3, length: 1, value: "x" },
			{ position: 0, length: 1, value: "p" },
			{ position: 3, length: 1, value: "x" }
		],
		[
			{ position: 3, length: 1, value: "x" },
			{ position: 0, length: 2, value: "pq" },
			{ position: 3, length: 1, value: "x" }
		],
		[
			{ position: 3, length: 1, value: "x" },
			{ position: 0, length: 0, value: "p" },
			{ position: 4, length: 1, value: "x" }
		],
		[
			{ position: 2, length: 0, value: "x" },
			{ position: 2, length: 0, value: "p" },
			{ position: 3, length: 0, value: "x" }
		],
		[
			{ position: 2, length: 0, value: "x" },
			{ position: 1, length: 2, value: "p" },
			{ position: 2, length: 0, value: "x" }
		],
		[
			{ position: 2, length: 0, value: "x" },
			{ position: 1, length: 1, value: "p" },
			{ position: 2, length: 0, value: "x" }
		],
		[
			{ position: 1, length: 1, value: "x" },
			{ position: 1, length: 1, value: "p" },
			{ position: 2, length: 0, value: "x" }
		],
		[
			{ position: 0, length: 2, value: "x" },
			{ position: 1, length: 2, value: "p" },
			{ position: 0, length: 1, value: "x" }
		],
		[
			{ position: 1, length: 2, value: "x" },
			{ position: 0, length: 2, value: "p" },
			{ position: 1, length: 1, value: "x" }
		],
		[
			{ position: 0, length: 4, value: "x" },
			{ position: 1, length: 2, value: "p" },
			{ position: 0, length: 3, value: "xp" }
		],
		[
			{ position: 0, length: 4, value: "x" },
			{ position: 2, length: 0, value: "p" },
			{ position: 0, length: 5, value: "xp" }
		],
		[
			{ position: 1, length: 2, value: "" },
			{ position: 0, length: 4, value: "p" },
			{ position: 1, length: 0, value: "" }
		],
		[
			{ position: 2, length: 2, value: "x" },
			{ position: 2, length: 0, value: "p" },
			{ position: 3, length: 2, value: "x" }
		]
	])("%o onto %o", (edit, prior, expected) => {
		expect(rebased(edit, prior)).toEqual(expected);
	});

	it("first", () => {
		const result = rebased(
			{ position: 2, length: 0, value: "x" },
			{ position: 2, length: 0, value: "p" },
			true
		);

		expect(result).toEqual({ position: 2, length: 0, value: "x" });
	});

	// The text both edits mean together, built without transforming anything,
	// so that it checks the transform rather than restating it: a character
	// survives unless either edit removed it, and each insertion sits before
	// the character it was typed in front of, the prior one first when both
	// were typed at the same place.
	it("exhaustive", () => {
		const edits: Array<TextEdit> = [];
		const priors: Array<TextEdit> = [];
		for (let position = 0; position <= TEXT.length; position++) {
			for (let end = position; end <= TEXT.length; end++) {
				["", "x", "xy"].forEach(value =>
					edits.push({ position, length: end - position, value })
				);
				["", "p", "pq"].forEach(value =>
					priors.push({ position, length: end - position, value })
				);
			}
		}

		edits.forEach(edit =>
			priors.forEach(prior => {
				let merged = "";
				for (let index = 0; index <= TEXT.length; index++) {
					if (index === prior.position) {
						merged += prior.value;
					}
					if (index === edit.position) {
						merged += edit.value;
					}

					const removedByEdit =
						index >= edit.position &&
						index < edit.position + edit.length;
					const removedByPrior =
						index >= prior.position &&
						index < prior.position + prior.length;
					if (
						index < TEXT.length &&
						!removedByEdit &&
						!removedByPrior
					) {
						merged += TEXT[index];
					}
				}

				const text = applied(
					applied(TEXT, prior),
					rebased(edit, prior)
				);

				expect(text, JSON.stringify({ edit, prior })).toBe(merged);
			})
		);
	});

	it("converges", () => {
		const locals: Array<TextEdit> = [];
		const remotes: Array<TextEdit> = [];
		for (let position = 0; position <= TEXT.length; position++) {
			for (let end = position; end <= TEXT.length; end++) {
				["", "x", "xy"].forEach(value =>
					locals.push({ position, length: end - position, value })
				);
				["", "p", "pq"].forEach(value =>
					remotes.push({ position, length: end - position, value })
				);
			}
		}

		locals.forEach(local =>
			remotes.forEach(remote => {
				const here = applied(
					applied(TEXT, local),
					rebased(remote, local, true)
				);
				const there = applied(
					applied(TEXT, remote),
					rebased(local, remote)
				);

				expect(here, JSON.stringify({ local, remote })).toBe(there);
			})
		);
	});
});
