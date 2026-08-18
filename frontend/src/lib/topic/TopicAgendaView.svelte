<script lang="ts">
	import { t } from "$lib/assets/translations";

	import IconDelete from "$lib/assets/icons/IconDelete.svelte";
	import IconPlus from "$lib/assets/icons/IconPlus.svelte";

	import type { TopicDetails } from "$lib/details/DetailTypes";
	import type { UpdateAction } from "$lib/editor/ActionTypes";
	import Input from "$lib/editor/Input.svelte";
	import TextArea from "$lib/editor/TextArea.svelte";
	import FeedbackButton from "$lib/form/FeedbackButton.svelte";
	import IconButton from "$lib/form/IconButton.svelte";

	import type {
		TopicCreateAction,
		TopicDeleteAction,
		TopicUpdateDescriptionAction,
		TopicUpdateDurationAction,
		TopicUpdateNameAction
	} from "./TopicTypes";
	import TopicWebSocketClient from "./TopicWebSocketClient";

	export type TopicAgendaViewProps = {
		meetingId: Readonly<number>;
		topics: Readonly<Array<TopicDetails>>;
	};

	let { meetingId, topics = $bindable() }: TopicAgendaViewProps = $props();

	function addTopic(): Promise<void> {
		const request: TopicCreateAction = {
			meetingId,
			sequenceId: topics.length,
			name: ""
		};

		TopicWebSocketClient.create(request);
		return Promise.resolve();
	}

	const handleUpdateTopicName = (topicId: number, action: UpdateAction) => {
		const request: TopicUpdateNameAction = {
			meetingId,
			topicId,
			action: "UPDATE_NAME",
			position: action.position,
			length: action.length,
			value: action.value
		};

		TopicWebSocketClient.update(request);
	};

	const handleUpdateTopicDescription = (
		topicId: number,
		action: UpdateAction
	) => {
		const request: TopicUpdateDescriptionAction = {
			meetingId,
			topicId,
			action: "UPDATE_DESCRIPTION",
			position: action.position,
			length: action.length,
			value: action.value
		};

		TopicWebSocketClient.update(request);
	};

	const handleUpdateTopicDuration = (
		topicId: number,
		duration: number | null
	) => {
		const request: TopicUpdateDurationAction = {
			meetingId,
			topicId,
			action: "UPDATE_DURATION",
			duration: duration
		};

		TopicWebSocketClient.update(request);
	};

	const handleDeleteTopic = (topicId: number) => {
		const request: TopicDeleteAction = {
			meetingId,
			topicId
		};

		TopicWebSocketClient.delete(request);
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

		<div class="duration">
			<label for="topic-{topic.id}-duration">
				{$t("common.duration")}
			</label>
			<input
				id="topic-{topic.id}-duration"
				type="number"
				min="1"
				bind:value={topic.duration}
				onchange={() =>
					handleUpdateTopicDuration(topic.id, topic.duration)}
			/>
			<span>{$t("common.minutesShort")}</span>
		</div>

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

	.duration {
		display: flex;
		align-items: center;
		gap: 0.3rem;
		color: #888;
		font-size: 0.9rem;
	}

	.duration input {
		width: 3rem;
		font-size: inherit;
		color: inherit;
		border: none;
		background: none;
		text-align: right;
	}

	.duration input:focus {
		outline: none;
	}
</style>
