export type SessionInfo = {
	id: number;
	accessToken: string;
	activeUntil: string;
};

export type SessionCreateRequest = {
	email: string;
	password: string;
};

export type SessionUpdateRequest = {
	organisationId: number;
}