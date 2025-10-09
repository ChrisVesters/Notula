import type { CreateUserRequest, UserInfo } from "./UserTypes";

export default class UserClient {
	public static async create(request: CreateUserRequest): Promise<UserInfo> {
		return fetch(getEndpoint(), {
			method: "POST",
			headers: {
				"Content-Type": "application/json"
			},
			body: JSON.stringify(request)
		}).then(res => res.json());
	}
}

function getEndpoint(): string {
	return `${import.meta.env.VITE_API_URL}/users`;
}
