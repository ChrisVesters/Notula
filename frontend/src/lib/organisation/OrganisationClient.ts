import Client from "$lib/common/Client";
import DataStorage from "$lib/common/DataStorage";

import type {
	CreateOrganisationRequest,
	OrganisationInfo
} from "./OrganisationTypes";

export default class OrganisationClient extends Client {
	public static async create(
		request: CreateOrganisationRequest
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
