import { cleanup, fireEvent, render } from "@testing-library/svelte";
import { afterEach, describe, expect, it } from "vitest";

import PasswordField from "$lib/form/PasswordField.svelte";

afterEach(cleanup);

function elements(container: HTMLElement) {
	return {
		input: container.querySelector("input") as HTMLInputElement,
		toggle: container.querySelector("button.adornment") as HTMLButtonElement
	};
}

function renderField(props: Record<string, unknown> = {}) {
	return render(PasswordField, {
		props: {
			value: "",
			label: "Secret",
			id: "secret",
			autocomplete: "current-password",
			...props
		}
	});
}

describe("PasswordField", () => {
	it("labels the input and shows the error", () => {
		const { getByLabelText, getByText } = renderField({ error: "Error!" });

		expect(getByLabelText(/Secret/)).toBeTruthy();
		expect(getByText("Error!")).toBeTruthy();
	});

	it("updates on input", async () => {
		const { container } = renderField();
		const { input } = elements(container);

		await fireEvent.input(input, { target: { value: "abc12345" } });

		expect(input.value).toBe("abc12345");
	});

	it("toggles visibility", async () => {
		const { container } = renderField({ value: "mypassword" });
		const { input, toggle } = elements(container);

		expect(input.type).toBe("password");

		await fireEvent.click(toggle);

		expect(input.type).toBe("text");

		await fireEvent.click(toggle);

		expect(input.type).toBe("password");
	});

	it("swaps the icon with the visibility", async () => {
		const { container } = renderField({ value: "mypassword" });
		const { toggle } = elements(container);

		const hidden = toggle.querySelectorAll("path").length;

		await fireEvent.click(toggle);

		expect(toggle.querySelectorAll("path").length).not.toBe(hidden);
	});
});
