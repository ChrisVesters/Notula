<script lang="ts">
	import IconDelete from "$lib/assets/icons/IconDelete.svelte";
	import IconPlus from "$lib/assets/icons/IconPlus.svelte";
	import { t } from "$lib/assets/translations";

	import type { TopicDetails } from "$lib/details/DetailTypes";
	import type { UpdateAction } from "$lib/editor/ActionTypes";
	import Input from "$lib/editor/Input.svelte";
	import TextArea from "$lib/editor/TextArea.svelte";
	import FeedbackButton from "$lib/form/FeedbackButton.svelte";
	import IconButton from "$lib/form/IconButton.svelte";

	import type {
		TopicUpdateDescriptionAction,
		TopicUpdateNameAction
	} from "./TopicTypes";
	import TopicWebSocketClient from "./TopicWebSocketClient";

	export type TopicAgendaViewProps = {
		meetingId: number;
		topics: Array<TopicDetails>;
	};

	let { meetingId, topics = $bindable() }: TopicAgendaViewProps = $props();

	function addTopic(): Promise<void> {
		const request = {
			name: ""
		};

		TopicWebSocketClient.create(meetingId, request);
		return Promise.resolve();
	}

	const handleUpdateTopicName = (topicId: number, action: UpdateAction) => {
		const request: TopicUpdateNameAction = {
			action: "UPDATE_NAME",
			position: action.position,
			length: action.length,
			value: action.value
		};

		TopicWebSocketClient.update(meetingId, topicId, request);
	};

	const handleUpdateTopicDescription = (
		topicId: number,
		action: UpdateAction
	) => {
		const request: TopicUpdateDescriptionAction = {
			action: "UPDATE_DESCRIPTION",
			position: action.position,
			length: action.length,
			value: action.value
		};

		TopicWebSocketClient.update(meetingId, topicId, request);
	};

	const handleDeleteTopic = (topicId: number) => {
		TopicWebSocketClient.delete(meetingId, topicId);
	};
</script>

<h2>{$t("common.agenda")}</h2>
<!-- TODO: regular button? Add ripple effect or some other feedback -->
<FeedbackButton className="primary" onClick={addTopic}>
	<span class="label">
		<IconPlus />
		{$t("common.addObject", { object: $t("common.topic") })}
	</span>
</FeedbackButton>

<!-- // TODO: move to topic View -->
{#each topics as topic}
	<div class="topic">
		<Input
			className="h2"
			bind:value={topic.name}
			placeholder={$t("common.untitled")}
			onAction={action => handleUpdateTopicName(topic.id, action)}
		/>

		<TextArea
			bind:value={topic.description}
			placeholder={$t("common.startTyping")}
			onAction={action => handleUpdateTopicDescription(topic.id, action)}
		/>

		<div class="actions">
			<IconButton
				icon={IconDelete}
				onClick={() => handleDeleteTopic(topic.id)}
			/>
		</div>
	</div>
{/each}

<style>
	.topic {
		position: relative;
		margin-top: 1rem;
	}

	.actions {
		position: absolute;
		top: 0;
		left: -1.5rem;
	}
</style>
