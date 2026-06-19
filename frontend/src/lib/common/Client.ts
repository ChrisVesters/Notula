import Session from "$lib/auth/Session";

export default abstract class Client {
	public static async fetchGet<V>(endpoint: string): Promise<V> {
		const response = await fetch(endpoint, this.getRequest());
		this.verifyStatus(response, 200);
		return response.json();
	}

	public static async fetchGetAuth<V>(endpoint: string): Promise<V> {
		const response = await this.callAuth(endpoint, this.getRequest());
		this.verifyStatus(response, 200);
		return response.json();
	}

	public static async fetchPost<U, V>(endpoint: string, body: U): Promise<V> {
		const response = await fetch(endpoint, this.postRequest(body));
		this.verifyStatus(response, 201);
		return response.json();
	}

	public static async fetchPostAuth<U, V>(
		endpoint: string,
		body: U
	): Promise<V> {
		const response = await this.callAuth(endpoint, this.postRequest(body));
		this.verifyStatus(response, 201);
		return response.json();
	}

	public static async fetchPut<U, V>(endpoint: string, body: U): Promise<V> {
		const response = await fetch(endpoint, this.putRequest(body));
		this.verifyStatus(response, 200);
		return response.json();
	}

	public static async fetchPutAuth<U, V>(
		endpoint: string,
		body: U
	): Promise<V> {
		const response = await this.callAuth(endpoint, this.putRequest(body));
		this.verifyStatus(response, 200);
		return response.json();
	}

	public static async fetchDel(endpoint: string): Promise<void> {
		const response = await fetch(endpoint, this.deleteRequest());
		this.verifyStatus(response, 204);
	}

	public static async fetchDelAuth(endpoint: string): Promise<void> {
		const response = await this.callAuth(endpoint, this.deleteRequest());
		this.verifyStatus(response, 204);
	}

	private static getRequest(): RequestInit {
		return {
			method: "GET"
		};
	}

	private static postRequest<U>(request: U): RequestInit {
		return {
			method: "POST",
			headers: {
				"Content-Type": "application/json"
			},
			body: JSON.stringify(request)
		};
	}

	private static putRequest<U>(request: U): RequestInit {
		return {
			method: "PUT",
			headers: {
				"Content-Type": "application/json"
			},
			body: JSON.stringify(request)
		};
	}

	private static deleteRequest(): RequestInit {
		return {
			method: "DELETE"
		};
	}

	// This method is rather stupid now!
	private static async call<V>(
		endpoint: string,
		init: RequestInit
	): Promise<Response> {
		return fetch(endpoint, init);
	}

	private static async callAuth<V>(
		endpoint: string,
		init: RequestInit
	): Promise<Response> {
		let response: Response = await this.fetchAuth(endpoint, init);

		if (response.status === 401) {
			await Session.refresh();
			response = await this.fetchAuth(endpoint, init);
		}

		return response;
	}

	private static fetchAuth(
		endpoint: string,
		init: RequestInit
	): Promise<Response> {
		return fetch(endpoint, {
			...init,
			headers: {
				...init.headers,
				Authorization: `Bearer ${Session.getAccessToken()}`
			}
		});
	}

	private static verifyStatus(response: Response, statusCode: number): void {
		if (response.status !== statusCode) {
			// TODO: error handling
			throw new Error(response.statusText);
		}
	}
}
