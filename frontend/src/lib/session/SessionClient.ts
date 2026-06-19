import Client from "$lib/common/Client";

import type {
	SessionCreateRequest,
	SessionInfo,
	SessionUpdateRequest
} from "./SessionTypes";

export default class SessionClient extends Client {
	public static create(request: SessionCreateRequest): Promise<SessionInfo> {
		return this.fetchPost(getEndpoint(), request);
	}

	public static update(
		id: number,
		request: SessionUpdateRequest
	): Promise<SessionInfo> {
		return this.fetchPutAuth(`${getEndpoint()}/${id}`, request);
	}

	public static delete(id: number): Promise<void> {
		return this.fetchDelAuth(`${getEndpoint()}/${id}`);
	}
}

function getEndpoint(): string {
	return `${import.meta.env.VITE_API_URL}/sessions`;
}
