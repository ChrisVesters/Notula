export type OrganisationUserView = {
	id: number;
	organisationId: number;
	userId: number;
	email: string;
};

export type OrganisationUserCreateRequest = {
	email: string;
};
