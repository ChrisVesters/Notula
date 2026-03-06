import { get } from "svelte/store";

import Auth from "$lib/auth/Auth";
import DataStorage from "$lib/common/DataStorage";
import type { SessionInfo } from "$lib/session/SessionTypes";

// TODO: rename to SessionManager
export default class Session {
	static readonly #GRACE_PERIOD_MS = 60_000;
	static #TIMER_ID: number | null = null;
	static #SESSION_ID: number | null = null;
	static #ACCESS_TOKEN: string | null = null;

	public static getId(): number {
		if (Session.#SESSION_ID === null) {
			throw new Error("No valid session");
		}

		return Session.#SESSION_ID;
	}

	public static getAccessToken(): string {
		if (Session.#ACCESS_TOKEN === null) {
			throw new Error("No valid session");
		}

		return Session.#ACCESS_TOKEN;
	}

	public static load(): void {
		const sessionId = Number(DataStorage.getItem("sessionId"));
		const accessToken = DataStorage.getItem("accessToken");
		if (!Number.isInteger(sessionId) || accessToken === null) {
			return;
		}

		Session.#SESSION_ID = sessionId;
		Session.#ACCESS_TOKEN = accessToken;

		Auth.updatePrincipal(accessToken);
		Session.scheduleRefresh();
	}

	public static update(session: SessionInfo): void {
		DataStorage.setItem("sessionId", session.id.toString());
		DataStorage.setItem("accessToken", session.accessToken);

		Session.#SESSION_ID = session.id;
		Session.#ACCESS_TOKEN = session.accessToken;

		Auth.updatePrincipal(session.accessToken);
		Session.scheduleRefresh();
	}

	public static delete(): void {
		DataStorage.removeItem("sessionId");
		DataStorage.removeItem("accessToken");

		Session.#SESSION_ID = null;
		Session.#ACCESS_TOKEN = null;

		Session.cancelScheduledRefresh();
		Auth.deletePrincipal();
	}

	public static async refresh(): Promise<void> {
		Session.cancelScheduledRefresh();

		if (Session.#SESSION_ID === null) {
			console.warn("No session found");
			return;
		}

		const endpoint = `${import.meta.env.VITE_API_URL}/sessions/${Session.#SESSION_ID}/refresh`;
		const response: Response = await fetch(endpoint, {
			method: "POST",
			credentials: "include"
		});

		if (response.ok) {
			const session: SessionInfo = await response.json();
			Session.update(session);
		} else {
			console.warn("Unable to refresh session");
		}
	}

	private static scheduleRefresh(): void {
		Session.cancelScheduledRefresh();

		const principal = get(Auth.getPrincipal());
		if (principal === null) {
			console.warn("No principal found.");
			return;
		}

		const offset = principal.expiresAt.getTime() - Date.now();
		Session.#TIMER_ID = setTimeout(() => {
			Session.refresh();
		}, offset - this.#GRACE_PERIOD_MS);
	}

	private static cancelScheduledRefresh() {
		if (Session.#TIMER_ID === null) {
			return;
		}

		clearTimeout(Session.#TIMER_ID);
		Session.#TIMER_ID = null;
	}
}
