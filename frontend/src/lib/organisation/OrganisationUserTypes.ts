export type OrganisationUserView = {
	id: number;
	organisationId: number;
	userId: number;
	email: string;
};

export type OrganisationUserInfo = {
	id: number;
	organisationId: number;
	userId: number;
};

export type OrganisationUserCreateRequest = {
	email: string;
};
