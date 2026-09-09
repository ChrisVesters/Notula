<script lang="ts">
	import { t } from "$lib/assets/translations";

	import type { MeetingDetails } from "$lib/details/DetailTypes";
	import type { UpdateAction } from "$lib/editor/ActionTypes";

	import Input from "$lib/editor/Input.svelte";
	import TextArea from "$lib/editor/TextArea.svelte";
	import MeetingWebSocketClient from "./MeetingWebSocketClient";

	export type MeetingInfoViewProps = {
		meeting: MeetingDetails;
	};

	let { meeting = $bindable() }: MeetingInfoViewProps = $props();

	const handleUpdateName = (edit: UpdateAction) => {
		MeetingWebSocketClient.send({ type: "RENAME_MEETING", ...edit });
	};

	const handleUpdateDescription = (edit: UpdateAction) => {
		MeetingWebSocketClient.send({ type: "DESCRIBE_MEETING", ...edit });
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
