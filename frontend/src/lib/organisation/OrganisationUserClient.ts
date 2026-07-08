import Client from "$lib/common/Client";
import type {
	OrganisationUserCreateRequest,
	OrganisationUserView
} from "./OrganisationUserTypes";

export default class OrganisationUserClient extends Client {
	public static getAll(): Promise<OrganisationUserView[]> {
		return this.fetchGetAuth(getEndpoint());
	}

	public static create(
		request: OrganisationUserCreateRequest
	): Promise<OrganisationUserView> {
		return this.fetchPostAuth(getEndpoint(), request);
	}
}

function getEndpoint(): string {
	return `${import.meta.env.VITE_API_URL}/organisation-users`;
}
