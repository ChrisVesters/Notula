import Client from "$lib/common/Client";
import DataStorage from "$lib/common/DataStorage";

import type {
	OrganisationCreateRequest,
	OrganisationInfo
} from "./OrganisationTypes";

export default class OrganisationClient extends Client {
	public static async getAll(): Promise<OrganisationInfo[]> {
		const token = DataStorage.getItem("accessToken");
		if (token == null) {
			return Promise.reject("No access token");
		}

		return this.get(getEndpoint(), token);
	}

	public static async create(
		request: OrganisationCreateRequest
	): Promise<OrganisationInfo> {
		const token = DataStorage.getItem("accessToken");
		if (token == null) {
			return Promise.reject("No access token");
		}

		return this.post(getEndpoint(), request, token);
	}
}

function getEndpoint(): string {
	return `${import.meta.env.VITE_API_URL}/organisations`;
}
