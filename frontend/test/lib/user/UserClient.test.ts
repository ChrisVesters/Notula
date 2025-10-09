import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import UserClient from "@/lib/user/UserClient";

const request: CreateUserRequest = { email: "carol@example.com", password: "pw" };
const response: UserInfo = { id: "123", email: "carol@example.com" };

// TODO: proper testing.
describe("UserClient.create", () => {
	const originalFetch = globalThis.fetch;

	beforeEach(() => {
		vi.resetAllMocks();
	});

	afterEach(() => {
		globalThis.fetch = originalFetch;
	});

	it("sends a POST with JSON body and returns parsed JSON on success", async () => {
		globalThis.fetch = vi.fn(() =>
			Promise.resolve({ json: () => Promise.resolve(response) })
		);

		const result = await UserClient.create(request);

		expect(globalThis.fetch).toHaveBeenCalledTimes(1);
		expect(globalThis.fetch).toHaveBeenCalledWith(
			expect.any(String),
			{
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify(req),
			}
		);

		expect(result).toEqual(fakeResponse);
	});

	it("rejects when fetch fails", async () => {
		globalThis.fetch = vi.fn(() => Promise.reject(new Error("network error")));

		const req = { email: "bob@example.com", password: "pw" };

		await expect(UserClient.create(req as any)).rejects.toThrow("network error");
	});

	it("propagates JSON parse errors", async () => {
		// simulate fetch resolving but json() throwing
		globalThis.fetch = vi.fn(() =>
			Promise.resolve({ json: () => Promise.reject(new Error("invalid json")) } as any)
		);

		const req = { email: "carol@example.com", password: "pw" };

		await expect(UserClient.create(req as any)).rejects.toThrow("invalid json");
	});
});