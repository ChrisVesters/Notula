import Client from "$lib/common/Client";

import type {
	SessionCreateRequest,
	SessionInfo,
	SessionUpdateRequest
} from "./SessionTypes";

export default class SessionClient extends Client {
	public static async create(
		request: SessionCreateRequest
	): Promise<SessionInfo> {
		return this.post(getEndpoint(), request);
	}

	public static async update(
		id: number,
		request: SessionUpdateRequest
	): Promise<SessionInfo> {
		return this.putAuthenticated(`${getEndpoint()}/${id}`, request);
	}

	public static async refresh(id: number): Promise<SessionInfo> {
		// TODO
		return fetch(`${getEndpoint()}/${id}/refresh`, {
			method: "POST",
			credentials: "include"
		}).then(res => res.json());
	}
}

function getEndpoint(): string {
	return `${import.meta.env.VITE_API_URL}/sessions`;
}
