import CLIENT_ID from "./ClientId";

export type EventOrigin = {
	userId: number;
	clientId: string | null;
};

export function isOwnEvent(origin: EventOrigin): boolean {
	return origin.clientId === CLIENT_ID;
}
