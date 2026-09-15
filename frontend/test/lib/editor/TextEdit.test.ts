import { describe, expect, it } from "vitest";

import { applied, moved } from "$lib/editor/TextEdit";

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
