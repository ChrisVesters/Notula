<script lang="ts">
	import IconDelete from "$lib/assets/icons/IconDelete.svelte";
	import IconDrag from "$lib/assets/icons/IconDrag.svelte";

	import { DropPosition, reorderHandler } from "$lib/common/ReorderHandler";
	import type { BlockDetails } from "$lib/details/DetailTypes";
	import IconButton from "$lib/form/IconButton.svelte";
	import TextBlockView from "$lib/textblock/TextBlockView.svelte";

	import {
		BlockType,
		type BlockDeleteAction,
		type BlockMoveAction
	} from "./BlockTypes";
	import BlockWebSocketClient from "./BlockWebSocketClient";

	export type BlockViewProps = {
		block: BlockDetails;
	};

	let { block = $bindable() }: BlockViewProps = $props();

	let dragged = $state(false);
	let dropPosition: DropPosition | null = $state(null);

	const handleMoveBlock = (sequenceId: number) => {
		const request: BlockMoveAction = {
			blockId: block.id,
			sequenceId
		};

		BlockWebSocketClient.move(request);
	};

	const handleReorder = $derived(
		reorderHandler({
			sequenceId: block.sequenceId,
			onDragChange: (value: boolean) => (dragged = value),
			onDropChange: (value: DropPosition | null) =>
				(dropPosition = value),
			onMove: handleMoveBlock
		})
	);

	const handleDeleteBlock = (blockId: number) => {
		const request: BlockDeleteAction = {
			blockId
		};

		BlockWebSocketClient.delete(request);
	};
</script>

<li
	class="block"
	class:dragged
	class:drop-before={dropPosition === DropPosition.BEFORE}
	class:drop-after={dropPosition === DropPosition.AFTER}
	{@attach handleReorder}
>
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
</li>

<style>
	.block {
		position: relative;
		margin-top: 1rem;
	}

	.block.dragged {
		opacity: 0.4;
	}

	.block.drop-before::before,
	.block.drop-after::after {
		content: "";

		position: absolute;
		left: 0;
		right: 0;

		border-top: 2px solid var(--color-primary-500);
	}

	.block.drop-before::before {
		top: -0.5rem;
	}

	.block.drop-after::after {
		bottom: -0.5rem;
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
