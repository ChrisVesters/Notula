<script lang="ts">
	import IconDelete from "$lib/assets/icons/IconDelete.svelte";
	import IconDrag from "$lib/assets/icons/IconDrag.svelte";

	import type { BlockDetails } from "$lib/details/DetailTypes";
	import IconButton from "$lib/form/IconButton.svelte";
	import TextBlockView from "$lib/textblock/TextBlockView.svelte";

	import MeetingWebSocketClient from "$lib/meeting/MeetingWebSocketClient";

	import { BlockType } from "./BlockTypes";

	export type BlockViewProps = {
		block: BlockDetails;
	};

	let { block = $bindable() }: BlockViewProps = $props();

	const handleDeleteBlock = (blockId: number) => {
		MeetingWebSocketClient.send({
			type: "REMOVE_BLOCK",
			block: blockId
		});
	};
</script>

<div class="block">
	{#if block.type === BlockType.TEXT}
		<TextBlockView blockId={block.id} bind:content={block} />
	{/if}

	<div class="actions">
		<button
			class="handle"
			type="button"
			aria-label="Move block"
			data-reorder-handle
		>
			<IconDrag />
		</button>
		<IconButton
			icon={IconDelete}
			onClick={() => handleDeleteBlock(block.id)}
		/>
	</div>
</div>

<style>
	.block {
		position: relative;
	}

	.actions {
		position: absolute;
		top: 0;
		left: -1.5rem;

		display: flex;
		flex-direction: column;
	}

	.handle {
		background: none;
		border: none;
		padding: 0;

		cursor: grab;
	}

	.handle:hover {
		color: var(--color-primary-500);
	}

	.handle:active {
		cursor: grabbing;
	}
</style>
