import { cleanup, render, screen } from "@testing-library/svelte";
import { tick } from "svelte";
import { afterEach, describe, expect, test } from "vitest";

import Input from "$lib/editor/Input.svelte";

afterEach(cleanup);

const renderInput = (value: string) =>
	render(Input, { props: { value, onAction: () => {} } });

const input = () => screen.getByRole("textbox") as HTMLInputElement;

const caretAt = (position: number) => {
	const element = input();
	element.focus();
	element.setSelectionRange(position, position);
};

describe("Input component", () => {
	test("keeps the caret when text is inserted before it", async () => {
		const { rerender } = renderInput("Hell world");
		caretAt(5);

		await rerender({ value: "Hello world" });
		await tick();

		expect(input().value).toBe("Hello world");
		expect(input().selectionStart).toBe(6);
	});

	test("keeps the caret when text is removed before it", async () => {
		const { rerender } = renderInput("Hello there world");
		caretAt(12);

		await rerender({ value: "Hello world" });
		await tick();

		expect(input().selectionStart).toBe(6);
	});

	test("leaves the caret alone when the change is after it", async () => {
		const { rerender } = renderInput("Hello world");
		caretAt(5);

		await rerender({ value: "Hello there" });
		await tick();

		expect(input().selectionStart).toBe(5);
	});

	test("applies the value when the input is not focused", async () => {
		const { rerender } = renderInput("Hello world");

		await rerender({ value: "Hello there" });
		await tick();

		expect(input().value).toBe("Hello there");
	});
});
