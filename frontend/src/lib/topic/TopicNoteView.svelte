<script lang="ts">
	import { t } from "$lib/assets/translations";

	import IconPlus from "$lib/assets/icons/IconPlus.svelte";

	import { BlockType } from "$lib/block/BlockTypes";
	import BlockView from "$lib/block/BlockView.svelte";
	import type { TopicDetails } from "$lib/details/DetailTypes";
	import type { UpdateAction } from "$lib/editor/ActionTypes";
	import Input from "$lib/editor/Input.svelte";
	import FeedbackButton from "$lib/form/FeedbackButton.svelte";

	import MeetingWebSocketClient from "$lib/meeting/MeetingWebSocketClient";

	export type TopicNoteViewProps = {
		topic: Readonly<TopicDetails>;
	};

	let { topic = $bindable() }: TopicNoteViewProps = $props();

	let blocks = $derived(
		topic.blocks.toSorted((a, b) => a.sequenceId - b.sequenceId)
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
			sequenceId: topic.blocks.length
		});

		return Promise.resolve();
	}
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

<ul class="blocks">
	{#each blocks as block, index (block.id)}
		<BlockView bind:block={blocks[index]} />
	{/each}
</ul>

<style>
	.blocks {
		list-style: none;
		margin: 0;
		padding: 0;
	}
</style>
