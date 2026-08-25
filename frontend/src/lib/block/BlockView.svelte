<script lang="ts">
	import IconDelete from "$lib/assets/icons/IconDelete.svelte";
	import IconDrag from "$lib/assets/icons/IconDrag.svelte";

	import type { BlockDetails } from "$lib/details/DetailTypes";
	import IconButton from "$lib/form/IconButton.svelte";
	import TextBlockView from "$lib/textblock/TextBlockView.svelte";

	import {
		blockDrag,
		type DragSource,
		type DropPosition
	} from "./BlockDrag.svelte";
	import {
		BlockType,
		type BlockDeleteAction,
		type BlockMoveAction
	} from "./BlockTypes";
	import BlockWebSocketClient from "./BlockWebSocketClient";

	export type BlockViewProps = {
		block: BlockDetails;
		topicId: Readonly<number>;
	};

	let { block = $bindable(), topicId }: BlockViewProps = $props();

	let draggable = $state(false);

	const dragged = $derived(blockDrag.source?.blockId === block.id);
	const dropPosition = $derived(
		blockDrag.target?.blockId === block.id
			? blockDrag.target.position
			: null
	);

	const handleDragStart = (event: DragEvent) => {
		if (!event.dataTransfer) {
			return;
		}

		event.dataTransfer.effectAllowed = "move";
		// Firefox only starts a drag once data has been set.
		event.dataTransfer.setData("text/plain", "");

		blockDrag.start({
			topicId,
			blockId: block.id,
			sequenceId: block.sequenceId
		});
	};

	const handleDragEnd = () => {
		draggable = false;
		blockDrag.end();
	};

	const handleDragOver = (
		event: DragEvent & { currentTarget: HTMLElement }
	) => {
		if (!event.dataTransfer || blockDrag.source?.topicId !== topicId) {
			return;
		}

		event.preventDefault();
		event.dataTransfer.dropEffect = "move";

		const bounds = event.currentTarget.getBoundingClientRect();
		const middle = bounds.top + bounds.height / 2;

		blockDrag.over({
			blockId: block.id,
			position: event.clientY < middle ? "BEFORE" : "AFTER"
		});
	};

	const handleDrop = (event: DragEvent) => {
		const source = blockDrag.source;
		const position = dropPosition;

		blockDrag.end();

		if (!source || position === null) {
			return;
		}

		event.preventDefault();

		handleMoveBlock(source, position);
	};

	const handleMoveBlock = (source: DragSource, position: DropPosition) => {
		// The index of the gap the block is dropped in, before removal.
		const gap =
			position === "BEFORE" ? block.sequenceId : block.sequenceId + 1;
		const sequenceId = gap > source.sequenceId ? gap - 1 : gap;

		if (sequenceId === source.sequenceId) {
			return;
		}

		const request: BlockMoveAction = {
			blockId: source.blockId,
			sequenceId
		};

		BlockWebSocketClient.move(request);
	};

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
	class:drop-before={dropPosition === "BEFORE"}
	class:drop-after={dropPosition === "AFTER"}
	{draggable}
	ondragstart={handleDragStart}
	ondragend={handleDragEnd}
	ondragover={handleDragOver}
	ondrop={handleDrop}
>
	{#if block.type === BlockType.TEXT}
		<TextBlockView blockId={block.id} bind:content={block} />
	{/if}

	<div class="actions">
		<button
			class="handle"
			type="button"
			aria-label="Move block"
			onpointerdown={() => (draggable = true)}
			onpointerup={() => (draggable = false)}
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
