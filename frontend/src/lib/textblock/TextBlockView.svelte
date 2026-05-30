<script lang="ts">
	import { t } from "$lib/assets/translations";

	import type {
		TextBlockContent
	} from "$lib/details/DetailTypes";
	import type { UpdateAction } from "$lib/editor/ActionTypes";
	import TextArea from "$lib/editor/TextArea.svelte";

	import type { TextBlockUpdateContentAction } from "./TextBlockTypes";
	import TextBlockWebSocketClient from "./TextBlockWebSocketClient";

	export type TextBlockViewProps = {
		meetingId: number;
		topicId: number;
		blockId: number;
		content: TextBlockContent;
	};

	const {
		meetingId,
		topicId,
		blockId,
		content = $bindable()
	}: TextBlockViewProps = $props();

	const handleUpdateContent = (action: UpdateAction) => {
		const request: TextBlockUpdateContentAction = {
			action: "UPDATE_CONTENT",
			position: action.position,
			length: action.length,
			value: action.value
		};

		TextBlockWebSocketClient.updateContent(
			meetingId,
			topicId,
			blockId,
			request
		);
	};
</script>

<TextArea
	bind:value={content.content}
	onAction={handleUpdateContent}
	placeholder={$t("common.startTyping")}
/>
