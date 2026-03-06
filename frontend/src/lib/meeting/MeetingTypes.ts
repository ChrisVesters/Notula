export type MeetingInfo = {
	id: number;
	name: string;
};

export type MeetingDetails = {
	info: MeetingInfo;
}

export type MeetingCreateRequest = {
	name: string;
};