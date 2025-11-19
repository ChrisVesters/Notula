import type { CreateSessionRequest, SessionInfo } from "./SessionTypes";

export default class SessionClient {
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
}

function getEndpoint(): string {
	return `${import.meta.env.VITE_API_URL}/sessions`;
}
