import Client from "$lib/common/Client";
import DataStorage from "$lib/common/DataStorage";

import type {
	CreateSessionRequest,
	SessionInfo,
	SessionUpdateRequest
} from "./SessionTypes";

export default class SessionClient extends Client {
	public static async create(
		request: CreateSessionRequest
	): Promise<SessionInfo> {
		return fetch(getEndpoint(), {
			method: "POST",
			headers: {
				"Content-Type": "application/json"
			},
			body: JSON.stringify(request)
		}).then(res => res.json());
	}

	public static async update(
		id: number,
		request: SessionUpdateRequest
	): Promise<SessionInfo> {
		const token = DataStorage.getItem("accessToken");
		if (token == null) {
			return Promise.reject("No access token");
		}

		return this.put(`${getEndpoint()}/${id}`, request, token);
	}
}

function getEndpoint(): string {
	return `${import.meta.env.VITE_API_URL}/sessions`;
}
