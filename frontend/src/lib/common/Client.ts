export default abstract class Client {
	public static async get<Response>(
		endpoint: string,
		token?: string
	): Promise<Response> {
		return fetch(endpoint, {
			method: "GET",
			headers: {
				"Authorization": `Bearer ${token}`
			}
		}).then(res => res.json());
	}

	public static async post<Request, Response>(
		endpoint: string,
		request: Request,
		token?: string
	): Promise<Response> {
		return fetch(endpoint, {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				Authorization: `Bearer ${token}`
			},
			body: JSON.stringify(request)
		}).then(res => res.json());
	}

	public static async put<Request, Response>(
		endpoint: string,
		request: Request,
		token?: string
	): Promise<Response> {
		return fetch(endpoint, {
			method: "PUT",
			headers: {
				"Content-Type": "application/json",
				"Authorization": `Bearer ${token}`
			},
			body: JSON.stringify(request)
		}).then(res => res.json());
	}
}
