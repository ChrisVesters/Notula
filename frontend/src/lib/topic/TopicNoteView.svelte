<script lang="ts">
	import { t } from "$lib/assets/translations";

	import IconPlus from "$lib/assets/icons/IconPlus.svelte";

	import { BlockType, type BlockCreateAction } from "$lib/block/BlockTypes";
	import BlockView from "$lib/block/BlockView.svelte";
	import BlockWebSocketClient from "$lib/block/BlockWebSocketClient";
	import type { TopicDetails } from "$lib/details/DetailTypes";
	import type { UpdateAction } from "$lib/editor/ActionTypes";
	import Input from "$lib/editor/Input.svelte";
	import FeedbackButton from "$lib/form/FeedbackButton.svelte";

	import type { TopicUpdateNameAction } from "./TopicTypes";
	import TopicWebSocketClient from "./TopicWebSocketClient";

	export type TopicNoteViewProps = {
		topic: Readonly<TopicDetails>;
	};

	let { topic = $bindable() }: TopicNoteViewProps = $props();

	const handleUpdateTopicName = (action: UpdateAction) => {
		const request: TopicUpdateNameAction = {
			topicId: topic.id,
			action: "UPDATE_NAME",
			position: action.position,
			length: action.length,
			value: action.value
		};

		TopicWebSocketClient.update(request);
	};

	function addBlock(): Promise<void> {
		const request: BlockCreateAction = {
			topicId: topic.id,
			type: BlockType.TEXT,
			sequenceId: topic.blocks.length
		};

		BlockWebSocketClient.create(request);

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

<!-- TODO: extract to component? -->
<!-- TODO: Sort -->
<!-- $: sortedItems = items
  .slice()
  .sort((a, b) => a.id - b.id); -->
{#each topic.blocks as block, index (block.id)}
	<BlockView bind:block={topic.blocks[index]} />
{/each}
