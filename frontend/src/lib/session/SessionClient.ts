import Client from "$lib/common/Client";

import type {
	SessionCreateRequest,
	SessionInfo,
	SessionUpdateRequest
} from "./SessionTypes";

export default class SessionClient extends Client {
	public static create(
		request: SessionCreateRequest
	): Promise<SessionInfo> {
		return this.post(getEndpoint(), request);
	}

	public static update(
		id: number,
		request: SessionUpdateRequest
	): Promise<SessionInfo> {
		return this.putAuthenticated(`${getEndpoint()}/${id}`, request);
	}
}

function getEndpoint(): string {
	return `${import.meta.env.VITE_API_URL}/sessions`;
}
