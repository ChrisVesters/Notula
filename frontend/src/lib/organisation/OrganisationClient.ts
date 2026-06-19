import Client from "$lib/common/Client";

import type {
	OrganisationCreateRequest,
	OrganisationInfo,
	OrganisationUpdateRequest
} from "./OrganisationTypes";

export default class OrganisationClient extends Client {
	public static getAll(): Promise<OrganisationInfo[]> {
		return this.fetchGetAuth(getEndpoint());
	}

	public static get(id: number): Promise<OrganisationInfo> {
		return this.fetchGetAuth(`${getEndpoint()}/${id}`);
	}

	public static create(
		request: OrganisationCreateRequest
	): Promise<OrganisationInfo> {
		return this.fetchPostAuth(getEndpoint(), request);
	}

	public static update(
		id: number,
		request: OrganisationUpdateRequest
	): Promise<OrganisationInfo> {
		return this.fetchPutAuth(`${getEndpoint()}/${id}`, request);
	}
}

function getEndpoint(): string {
	return `${import.meta.env.VITE_API_URL}/organisations`;
}
