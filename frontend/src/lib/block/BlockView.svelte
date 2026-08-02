<script lang="ts">
	import IconDelete from "$lib/assets/icons/IconDelete.svelte";

	import type { BlockDetails } from "$lib/details/DetailTypes";
	import IconButton from "$lib/form/IconButton.svelte";
	import TextBlockView from "$lib/textblock/TextBlockView.svelte";

	import { BlockType } from "./BlockTypes";
	import BlockWebSocketClient from "./BlockWebSocketClient";

	export type BlockViewProps = {
		meetingId: number;
		topicId: number;
		block: BlockDetails;
	};

	let { meetingId, topicId, block = $bindable() }: BlockViewProps = $props();

	const handleDeleteBlock = (blockId: number) => {
		BlockWebSocketClient.delete(meetingId, topicId, blockId);
	};
</script>

<div class="block">
	{#if block.type === BlockType.TEXT}
		<TextBlockView
			{meetingId}
			{topicId}
			blockId={block.id}
			bind:content={block}
		/>
	{/if}

	<div class="actions">
		<IconButton
			icon={IconDelete}
			onClick={() => handleDeleteBlock(block.id)}
		/>
	</div>
</div>

<style>
	.block {
		position: relative;
		margin-top: 1rem;
	}

	.actions {
		position: absolute;
		top: 0;
		left: -1.5rem;
	}
</style>
