export default class DataStorage {
	static setItem(key: string, value: string): void {
		localStorage.setItem(key, value);
	}

	static getItem(key: string): string | null {
		return localStorage.getItem(key);
	}

	static removeItem(key: string): void {
		localStorage.removeItem(key);
	}

	static clear(): void {
		localStorage.clear();
	}
}