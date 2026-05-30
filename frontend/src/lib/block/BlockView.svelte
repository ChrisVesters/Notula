<script lang="ts">
	import type { BlockDetails } from "$lib/details/DetailTypes";
	import TextBlockView from "$lib/textblock/TextBlockView.svelte";

	import { BlockType } from "./BlockTypes";

	export type BlockViewProps = {
		meetingId: number;
		topicId: number;
		block: BlockDetails;
	};

	let {
		meetingId,
		topicId,
		block = $bindable()
	}: BlockViewProps = $props();
</script>

{#if block.type === BlockType.TEXT}
	<TextBlockView {meetingId} {topicId} blockId={block.id} bind:content={block} />
{/if}
