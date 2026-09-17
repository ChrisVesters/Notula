import { cleanup, fireEvent, render, screen } from "@testing-library/svelte";
import { afterEach, describe, expect, test } from "vitest";

import TextField from "$lib/form/TextField.svelte";

afterEach(cleanup);

describe("TextField component", () => {
	test("renders label and required asterisk", () => {
		render(TextField, {
			props: {
				value: "foo",
				label: "Name",
				id: "name",
				type: "text",
				required: true,
				autocomplete: "name"
			}
		});

		const label = screen.getByText("Name");
		expect(label).toBeTruthy();

		// the asterisk should be present as .sup inside the label
		const sup = label.querySelector(".sup");
		expect(sup).toBeTruthy();
		expect(sup?.textContent).toBe("*");
	});

	test("input has correct attributes and initial value", () => {
		render(TextField, {
			props: {
				value: "hello",
				label: "Email",
				id: "email",
				type: "email",
				required: false,
				autocomplete: "email"
			}
		});

		const input = screen.getByRole("textbox") as HTMLInputElement;

		expect(input.value).toBe("hello");
		expect(input.type).toBe("email");
		expect(input.getAttribute("autocomplete")).toBe("email");
	});

	test("updates value on user input", async () => {
		render(TextField, {
			props: {
				value: "",
				label: "Title",
				id: "title",
				type: "text",
				autocomplete: "off"
			}
		});

		const input = document.getElementById("title") as HTMLInputElement;
		expect(input).toBeTruthy();

		await fireEvent.input(input, { target: { value: "typed text" } });
		expect(input.value).toBe("typed text");
	});

	test("shows error message when provided", () => {
		render(TextField, {
			props: {
				value: "",
				label: "Field",
				id: "field",
				type: "text",
				error: "Required",
				autocomplete: "off"
			}
		});

		const err = document.querySelector(".error-message");
		expect(err).toBeTruthy();
		expect(err?.textContent).toContain("Required");
	});

	test("no adornment buttons when not provided", () => {
		render(TextField, {
			props: {
				value: "",
				label: "Plain",
				id: "plain",
				type: "text",
				autocomplete: "off"
			}
		});

		const buttons = screen.queryAllByRole("button");
		// There should be no adornment buttons rendered
		expect(buttons.length).toBe(0);
	});
});
