export type OrganisationUserView = {
	id: number;
	organisationId: number;
	userId: number;
	email: string;
	role: OrganisationUserRole;
};

export type OrganisationUserInfo = {
	id: number;
	organisationId: number;
	userId: number;
	role: OrganisationUserRole;
};

export type OrganisationUserCreateRequest = {
	email: string;
	role: OrganisationUserRole;
};

export const OrganisationUserRole = {
	ADMIN: "ADMIN",
	MEMBER: "MEMBER"
} as const;

export type OrganisationUserRole =
	(typeof OrganisationUserRole)[keyof typeof OrganisationUserRole];
