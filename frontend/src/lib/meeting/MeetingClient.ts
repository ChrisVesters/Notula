import Client from "$lib/common/Client";

import type { MeetingCreateRequest, MeetingInfo } from "./MeetingTypes";

export default class MeetingClient extends Client {
	public static getAll(): Promise<MeetingInfo[]> {
		return this.fetchGetAuth(getEndpoint());
	}

	public static create(request: MeetingCreateRequest): Promise<MeetingInfo> {
		return this.fetchPostAuth(getEndpoint(), request);
	}

	public static delete(id: number): Promise<void> {
		return this.fetchDelAuth(`${getEndpoint()}/${id}`);
	}
}

function getEndpoint(): string {
	return `${import.meta.env.VITE_API_URL}/meetings`;
}
