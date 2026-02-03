import Client from "$lib/common/Client";
import DataStorage from "$lib/common/DataStorage";

import type {
	MeetingCreateRequest,
	MeetingInfo
} from "./MeetingTypes";

export default class MeetingClient extends Client {
	public static async getAll(): Promise<MeetingInfo[]> {
		return this.getAuthenticated(getEndpoint());
	}

	public static async create(
		request: MeetingCreateRequest
	): Promise<MeetingInfo> {
		return this.postAuthenticated(getEndpoint(), request);
	}
}

function getEndpoint(): string {
	return `${import.meta.env.VITE_API_URL}/meetings`;
}
