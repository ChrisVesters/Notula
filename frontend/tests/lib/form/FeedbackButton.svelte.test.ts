import { cleanup, fireEvent, render, waitFor } from "@testing-library/svelte";
import { createRawSnippet } from "svelte";
import { afterEach, describe, expect, it } from "vitest";

import FeedbackButton from "$lib/form/FeedbackButton.svelte";

afterEach(cleanup);

function label(text: string) {
	return createRawSnippet(() => ({ render: () => `<span>${text}</span>` }));
}

function deferred() {
	let resolve: () => void = () => {};
	const promise = new Promise<void>(res => {
		resolve = () => res();
	});

	return { promise, resolve };
}

describe("FeedbackButton", () => {
	it("renders its children", () => {
		const { getByText } = render(FeedbackButton, {
			props: { onClick: () => Promise.resolve(), children: label("Save") }
		});

		expect(getByText("Save")).toBeTruthy();
	});

	it("is disabled and busy until the click settles", async () => {
		const pending = deferred();
		const { container } = render(FeedbackButton, {
			props: { onClick: () => pending.promise, children: label("Save") }
		});
		const button = container.querySelector("button") as HTMLButtonElement;

		expect(button.disabled).toBe(false);
		expect(button.getAttribute("aria-busy")).toBe("false");

		await fireEvent.click(button);

		expect(button.disabled).toBe(true);
		expect(button.getAttribute("aria-busy")).toBe("true");

		pending.resolve();

		await waitFor(() => expect(button.disabled).toBe(false));
		expect(button.getAttribute("aria-busy")).toBe("false");
	});

	it("keeps the spinner hidden until it is pending", async () => {
		const pending = deferred();
		const { container } = render(FeedbackButton, {
			props: { onClick: () => pending.promise, children: label("Save") }
		});
		const button = container.querySelector("button") as HTMLButtonElement;
		const spinner = container.querySelector(".spinner") as HTMLElement;

		expect(spinner.style.visibility).toBe("hidden");

		await fireEvent.click(button);

		expect(spinner.style.visibility).toBe("visible");

		pending.resolve();

		await waitFor(() => expect(spinner.style.visibility).toBe("hidden"));
	});

	it("ignores a second click while the first is pending", async () => {
		const pending = deferred();
		let calls = 0;
		const { container } = render(FeedbackButton, {
			props: {
				onClick: () => {
					calls++;

					return pending.promise;
				},
				children: label("Save")
			}
		});
		const button = container.querySelector("button") as HTMLButtonElement;

		await fireEvent.click(button);
		await fireEvent.click(button);

		expect(calls).toBe(1);

		pending.resolve();
	});
});
