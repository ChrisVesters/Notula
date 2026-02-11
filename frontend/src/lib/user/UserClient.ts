import Client from "$lib/common/Client";

import type { UserCreateRequest, UserInfo } from "./UserTypes";

export default class UserClient extends Client {
	public static async create(request: UserCreateRequest): Promise<UserInfo> {
		return this.post(getEndpoint(), request);
	}
}

function getEndpoint(): string {
	return `${import.meta.env.VITE_API_URL}/users`;
}
