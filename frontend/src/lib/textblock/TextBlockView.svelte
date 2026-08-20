<script lang="ts">
	import { t } from "$lib/assets/translations";

	import type { TextBlockContent } from "$lib/details/DetailTypes";
	import type { UpdateAction } from "$lib/editor/ActionTypes";
	import TextArea from "$lib/editor/TextArea.svelte";

	import type { TextBlockUpdateContentAction } from "./TextBlockTypes";
	import TextBlockWebSocketClient from "./TextBlockWebSocketClient";

	// TODO: Why not blockDetails?
	export type TextBlockViewProps = {
		blockId: number;
		content: TextBlockContent;
	};

	const { blockId, content = $bindable() }: TextBlockViewProps = $props();

	const handleUpdateContent = (action: UpdateAction) => {
		const request: TextBlockUpdateContentAction = {
			blockId,
			action: "UPDATE_CONTENT",
			position: action.position,
			length: action.length,
			value: action.value
		};

		TextBlockWebSocketClient.updateContent(request);
	};
</script>

<TextArea
	bind:value={content.content}
	onAction={handleUpdateContent}
	placeholder={$t("common.startTyping")}
/>
