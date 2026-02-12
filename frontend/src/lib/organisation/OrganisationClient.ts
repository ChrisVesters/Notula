import Client from "$lib/common/Client";

import type {
	OrganisationCreateRequest,
	OrganisationInfo
} from "./OrganisationTypes";

export default class OrganisationClient extends Client {
	public static getAll(): Promise<OrganisationInfo[]> {
		return this.getAuthenticated(getEndpoint());
	}

	public static create(
		request: OrganisationCreateRequest
	): Promise<OrganisationInfo> {
		return this.postAuthenticated(getEndpoint(), request);
	}
}

function getEndpoint(): string {
	return `${import.meta.env.VITE_API_URL}/organisations`;
}
