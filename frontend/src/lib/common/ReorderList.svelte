<script lang="ts" generics="T extends { id: number }">
	import type { Snippet } from "svelte";

	import type { DropTarget } from "$lib/common/ReorderHandler";
	import { DropPosition, reorderHandler } from "$lib/common/ReorderHandler";

	type ReorderListProps = {
		items: ReadonlyArray<T>;
		onMove: (id: number, afterId: number | null) => void;
		item: Snippet<[T, number]>;
	};

	let { items, onMove, item }: ReorderListProps = $props();

	let dragged: number | null = $state(null);
	let target: DropTarget | null = $state(null);

	const handleReorder = reorderHandler({
		order: () => items.map(entry => entry.id),
		onDragChange: (id: number | null) => (dragged = id),
		onDropChange: (value: DropTarget | null) => (target = value),
		onMove: (id: number, afterId: number | null) => onMove(id, afterId)
	});

	const dropAt = (id: number, position: DropPosition) =>
		target?.id === id && target.position === position;
</script>

<ul class="list" {@attach handleReorder}>
	{#each items as entry, index (entry.id)}
		<li
			class="item"
			class:dragged={dragged === entry.id}
			class:drop-before={dropAt(entry.id, DropPosition.BEFORE)}
			class:drop-after={dropAt(entry.id, DropPosition.AFTER)}
			data-reorder-id={entry.id}
		>
			{@render item(entry, index)}
		</li>
	{/each}
</ul>

<style>
	.list {
		list-style: none;
		margin: 0;
		padding: 0;
	}

	.item {
		position: relative;
		margin-top: 1rem;
	}

	.item.dragged {
		opacity: 0.4;
	}

	.item.drop-before::before,
	.item.drop-after::after {
		content: "";

		position: absolute;
		left: 0;
		right: 0;

		border-top: 2px solid var(--color-primary-500);
	}

	.item.drop-before::before {
		top: -0.5rem;
	}

	.item.drop-after::after {
		bottom: -0.5rem;
	}
</style>
