<script lang="ts">
	import { t } from "$lib/assets/translations";

	import IconPlus from "$lib/assets/icons/IconPlus.svelte";

	import { BlockType } from "$lib/block/BlockTypes";
	import BlockView from "$lib/block/BlockView.svelte";
	import ReorderList from "$lib/common/ReorderList.svelte";
	import type { BlockDetails, TopicDetails } from "$lib/details/DetailTypes";
	import type { UpdateAction } from "$lib/editor/ActionTypes";
	import Input from "$lib/editor/Input.svelte";
	import FeedbackButton from "$lib/form/FeedbackButton.svelte";

	import { Rank } from "$lib/common/Rank";
	import MeetingWebSocketClient from "$lib/meeting/MeetingWebSocketClient";

	export type TopicNoteViewProps = {
		topic: Readonly<TopicDetails>;
	};

	let { topic = $bindable() }: TopicNoteViewProps = $props();

	let blocks = $derived(
		topic.blocks.toSorted((a, b) => Rank.compare(a.rank, b.rank))
	);

	const handleUpdateTopicName = (edit: UpdateAction) => {
		MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: topic.id,
			...edit
		});
	};

	function addBlock(): Promise<void> {
		MeetingWebSocketClient.send({
			type: "ADD_BLOCK",
			topic: topic.id,
			blockType: BlockType.TEXT,
			afterId: blocks.at(-1)?.id ?? null
		});

		return Promise.resolve();
	}

	const handleMoveBlock = (blockId: number, afterId: number | null) => {
		MeetingWebSocketClient.send({
			type: "MOVE_BLOCK",
			block: blockId,
			afterId
		});
	};
</script>

<Input
	className="h2"
	bind:value={topic.name}
	placeholder={$t("common.untitled")}
	onAction={handleUpdateTopicName}
/>
<FeedbackButton className="primary" onClick={addBlock}>
	<span class="label">
		<IconPlus />
		{$t("common.addObject", { object: $t("common.note") })}
	</span>
</FeedbackButton>

<ReorderList items={blocks} onMove={handleMoveBlock}>
	{#snippet item(_: BlockDetails, index: number)}
		<BlockView bind:block={blocks[index]} />
	{/snippet}
</ReorderList>
