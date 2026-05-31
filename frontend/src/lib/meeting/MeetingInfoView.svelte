<script lang="ts">
	import { t } from "$lib/assets/translations";

	import type { MeetingDetails } from "$lib/details/DetailTypes";
	import type { UpdateAction } from "$lib/editor/ActionTypes";

	import Input from "$lib/editor/Input.svelte";
	import TextArea from "$lib/editor/TextArea.svelte";
	import type { MeetingUpdateDescriptionAction, MeetingUpdateNameAction } from "./MeetingTypes";
	import MeetingWebSocketClient from "./MeetingWebSocketClient";

	export type MeetingInfoViewProps = {
		meeting: MeetingDetails;
	};

	let { meeting = $bindable() }: MeetingInfoViewProps = $props();

	const handleUpdateName = (action: UpdateAction) => {
		const request: MeetingUpdateNameAction = {
			action: "UPDATE_NAME",
			position: action.position,
			length: action.length,
			value: action.value
		};

		MeetingWebSocketClient.updateName(meeting.id, request);
	};

	const handleUpdateDescription = (action: UpdateAction) => {
		const request: MeetingUpdateDescriptionAction = {
			action: "UPDATE_DESCRIPTION",
			position: action.position,
			length: action.length,
			value: action.value
		};

		MeetingWebSocketClient.updateDescription(meeting.id, request);
	};
</script>

<Input
	className="h1"
	bind:value={meeting.name}
	placeholder={$t("common.untitled")}
	onAction={handleUpdateName}
/>

<TextArea
	bind:value={meeting.description}
	placeholder={$t("common.startTyping")}
	onAction={handleUpdateDescription}
/>
