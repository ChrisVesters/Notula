<script lang="ts">
	import { t } from "$lib/assets/translations";

	import type { TextBlockContent } from "$lib/details/DetailTypes";
	import type { UpdateAction } from "$lib/editor/ActionTypes";
	import TextArea from "$lib/editor/TextArea.svelte";

	import MeetingWebSocketClient from "$lib/meeting/MeetingWebSocketClient";

	// TODO: Why not blockDetails?
	export type TextBlockViewProps = {
		blockId: number;
		content: TextBlockContent;
	};

	const { blockId, content = $bindable() }: TextBlockViewProps = $props();

	const handleUpdateContent = (edit: UpdateAction) => {
		MeetingWebSocketClient.send({
			type: "EDIT_TEXT_BLOCK",
			block: blockId,
			...edit
		});
	};
</script>

<TextArea
	bind:value={content.content}
	onAction={handleUpdateContent}
	placeholder={$t("common.startTyping")}
/>
