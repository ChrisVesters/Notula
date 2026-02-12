import { writable } from "svelte/store";
import type { Writable } from "svelte/store";

import DataStorage from "$lib/common/DataStorage";

export class Principal {
	readonly userId: number;
	readonly organisationId: number | null;
	readonly expiresAt: Date;

	constructor(
		userId: number,
		organisationId: number | null,
		expiresAt: Date
	) {
		this.userId = userId;
		this.organisationId = organisationId;
		this.expiresAt = expiresAt;
	}

	public hasExpired(): boolean {
		return this.expiresAt < new Date();
	}

	public isValid(): boolean {
		return !this.hasExpired();
	}

	public isScoped(): boolean {
		return this.isValid() && this.organisationId !== null;
	}
}

type JwtPayload = {
	sub: string;
	exp: number;
	iat: number;

	organisation_id?: number;
};

const principal = writable<Principal | null>(null);

export default class Auth {
	public static getPrincipal(): Writable<Principal | null> {
		return principal;
	}

	public static updatePrincipal(token: string): void {
		const base64Payload = token.split(".")[1];
		const payload: JwtPayload = JSON.parse(window.atob(base64Payload));

		const userId = parseInt(payload.sub, 10);
		const organisationId = payload.organisation_id ?? null;
		const expiresAt = new Date(payload.exp * 1000);

		principal.set(new Principal(userId, organisationId, expiresAt));
	}

	public static deletePrincipal(): void {
		principal.set(null);
	}
}
