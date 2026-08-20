<script lang="ts">
	import IconDelete from "$lib/assets/icons/IconDelete.svelte";

	import type { BlockDetails } from "$lib/details/DetailTypes";
	import IconButton from "$lib/form/IconButton.svelte";
	import TextBlockView from "$lib/textblock/TextBlockView.svelte";

	import { BlockType, type BlockDeleteAction } from "./BlockTypes";
	import BlockWebSocketClient from "./BlockWebSocketClient";

	export type BlockViewProps = {
		block: BlockDetails;
	};

	let { block = $bindable() }: BlockViewProps = $props();

	const handleDeleteBlock = (blockId: number) => {
		const request: BlockDeleteAction = {
			blockId
		};

		BlockWebSocketClient.delete(request);
	};
</script>

<div class="block">
	{#if block.type === BlockType.TEXT}
		<TextBlockView blockId={block.id} bind:content={block} />
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
