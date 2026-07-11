import Client from "$lib/common/Client";
import type {
	OrganisationUserCreateRequest,
	OrganisationUserInfo,
	OrganisationUserView
} from "./OrganisationUserTypes";

export default class OrganisationUserClient extends Client {
	public static getAll(): Promise<OrganisationUserView[]> {
		return this.fetchGetAuth(getEndpoint());
	}

	public static create(
		request: OrganisationUserCreateRequest
	): Promise<OrganisationUserInfo> {
		return this.fetchPostAuth(getEndpoint(), request);
	}
}

function getEndpoint(): string {
	return `${import.meta.env.VITE_API_URL}/organisation-users`;
}
