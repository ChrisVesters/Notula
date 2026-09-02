import type { Writable } from "svelte/store";
import { writable } from "svelte/store";

import type { OrganisationUserRole } from "$lib/organisation/OrganisationUserTypes";

export class Principal {
	readonly userId: number;
	readonly organisationId: number | null;
	readonly role: OrganisationUserRole | null;
	readonly expiresAt: Date;

	constructor(
		userId: number,
		organisationId: number | null,
		role: OrganisationUserRole | null,
		expiresAt: Date
	) {
		this.userId = userId;
		this.organisationId = organisationId;
		this.role = role;
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

	public isSameIdentity(other: Principal): boolean {
		return (
			this.userId === other.userId &&
			this.organisationId === other.organisationId &&
			this.role === other.role
		);
	}
}

type JwtPayload = {
	sub: string;
	exp: number;
	iat: number;

	organisation_id?: number;
	role?: OrganisationUserRole;
};

const principal = writable<Principal | null>(null);

export default class Auth {
	public static getPrincipal(): Writable<Principal | null> {
		return principal;
	}

	public static updatePrincipal(token: string): Principal {
		const base64Payload = token.split(".")[1];
		const payload: JwtPayload = JSON.parse(window.atob(base64Payload));

		const userId = Number.parseInt(payload.sub, 10);
		const organisationId = payload.organisation_id ?? null;
		const role = payload.role ?? null;
		const expiresAt = new Date(payload.exp * 1000);

		const updated = new Principal(userId, organisationId, role, expiresAt);
		principal.set(updated);

		return updated;
	}

	public static deletePrincipal(): void {
		principal.set(null);
	}
}
