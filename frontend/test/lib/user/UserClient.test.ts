import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import UserClient from "$lib/user/UserClient";
import type { UserCreateRequest, UserInfo } from "$lib/user/UserTypes";

const request: UserCreateRequest = {
	email: "carol@example.com",
	password: "pw"
};

const response: UserInfo = { id: 123, email: "carol@example.com" };

function respondWith(body: Partial<Response>) {
	globalThis.fetch = vi.fn(() => Promise.resolve(body as Response));
}

describe("UserClient.create", () => {
	const originalFetch = globalThis.fetch;

	beforeEach(() => {
		vi.resetAllMocks();
	});

	afterEach(() => {
		globalThis.fetch = originalFetch;
	});

	it("posts the request as JSON and returns the parsed body", async () => {
		respondWith({ status: 201, json: () => Promise.resolve(response) });

		const result = await UserClient.create(request);

		expect(globalThis.fetch).toHaveBeenCalledTimes(1);
		expect(globalThis.fetch).toHaveBeenCalledWith(expect.any(String), {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(request)
		});
		expect(result).toEqual(response);
	});

	it("rejects when fetch fails", async () => {
		globalThis.fetch = vi.fn(() =>
			Promise.reject(new Error("network error"))
		);

		await expect(UserClient.create(request)).rejects.toThrow(
			"network error"
		);
	});

	it("rejects when the status is not created", async () => {
		respondWith({
			status: 409,
			statusText: "Conflict",
			json: () => Promise.resolve(response)
		});

		await expect(UserClient.create(request)).rejects.toThrow("Conflict");
	});

	it("propagates a body that will not parse", async () => {
		respondWith({
			status: 201,
			json: () => Promise.reject(new Error("invalid json"))
		});

		await expect(UserClient.create(request)).rejects.toThrow(
			"invalid json"
		);
	});
});
