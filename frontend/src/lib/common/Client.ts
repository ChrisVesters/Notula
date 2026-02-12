import Session from "$lib/auth/Session";

export default abstract class Client {
	public static get<V>(endpoint: string): Promise<V> {
		return this.call(endpoint, this.getRequest());
	}

	public static getAuthenticated<V>(endpoint: string): Promise<V> {
		return this.callAuthenticated(endpoint, this.getRequest());
	}

	public static post<U, V>(endpoint: string, request: U): Promise<V> {
		return this.call(endpoint, this.postRequest(request));
	}

	public static postAuthenticated<U, V>(
		endpoint: string,
		request: U
	): Promise<V> {
		return this.callAuthenticated(endpoint, this.postRequest(request));
	}

	public static put<U, V>(endpoint: string, request: U): Promise<V> {
		return this.call(endpoint, this.putRequest(request));
	}

	public static putAuthenticated<U, V>(
		endpoint: string,
		request: U
	): Promise<V> {
		return this.callAuthenticated(endpoint, this.putRequest(request));
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

	private static async call<V>(
		endpoint: string,
		init: RequestInit
	): Promise<V> {
		const response: Response = await fetch(endpoint, init);
		return this.processResult(response);
	}

	private static async callAuthenticated<V>(
		endpoint: string,
		init: RequestInit
	): Promise<V> {
		let response: Response = await this.fetchAuthenticated(endpoint, init);

		if (response.status === 401) {
			await Session.refresh();
			response = await this.fetchAuthenticated(endpoint, init);
		}

		return this.processResult(response);
	}

	private static fetchAuthenticated(
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

	private static processResult<V>(response: Response): Promise<V> {
		if (response.ok) {
			return response.json();
		} else {
			// TODO: error handling
			return Promise.reject();
		}
	}
}
