import DataStorage from "./DataStorage";

export default abstract class Client {
	public static async get<V>(endpoint: string): Promise<V> {
		return fetch(endpoint, {
			method: "GET"
		}).then(res => res.json());
	}

	public static async getAuthenticated<V>(endpoint: string): Promise<V> {
		const token = DataStorage.getItem("accessToken");
		if (token == null) {
			return Promise.reject("No access token");
		}

		return fetch(endpoint, {
			method: "GET",
			headers: {
				Authorization: `Bearer ${token}`
			}
		}).then(res => res.json());
	}

	public static async post<U, V>(endpoint: string, request: U): Promise<V> {
		return fetch(endpoint, {
			method: "POST",
			headers: {
				"Content-Type": "application/json"
			},
			body: JSON.stringify(request)
		}).then(res => res.json());
	}

	public static async postAuthenticated<U, V>(
		endpoint: string,
		request: U
	): Promise<V> {
		const token = DataStorage.getItem("accessToken");
		if (token == null) {
			return Promise.reject("No access token");
		}

		return fetch(endpoint, {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				Authorization: `Bearer ${token}`
			},
			body: JSON.stringify(request)
		}).then(res => res.json());
	}

	public static async put<U, V>(endpoint: string, request: U): Promise<V> {
		return fetch(endpoint, {
			method: "PUT",
			headers: {
				"Content-Type": "application/json"
			},
			body: JSON.stringify(request)
		}).then(res => res.json());
	}

	public static async putAuthenticated<U, V>(
		endpoint: string,
		request: U
	): Promise<V> {
		const token = DataStorage.getItem("accessToken");
		if (token == null) {
			return Promise.reject("No access token");
		}

		return fetch(endpoint, {
			method: "PUT",
			headers: {
				"Content-Type": "application/json",
				Authorization: `Bearer ${token}`
			},
			body: JSON.stringify(request)
		}).then(res => res.json());
	}
}
