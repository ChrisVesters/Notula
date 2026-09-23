<script lang="ts">
	import { t } from "$lib/assets/translations";

	import IconDelete from "$lib/assets/icons/IconDelete.svelte";
	import IconDrag from "$lib/assets/icons/IconDrag.svelte";

	import type { TopicDetails } from "$lib/details/DetailTypes";
	import type { UpdateAction } from "$lib/editor/ActionTypes";
	import Input from "$lib/editor/Input.svelte";
	import TextArea from "$lib/editor/TextArea.svelte";
	import IconButton from "$lib/form/IconButton.svelte";

	import MeetingWebSocketClient from "$lib/meeting/MeetingWebSocketClient";

	export type TopicAgendaViewProps = {
		topic: Readonly<TopicDetails>;
	};

	let { topic = $bindable() }: TopicAgendaViewProps = $props();

	const handleUpdateTopicName = (edit: UpdateAction) => {
		MeetingWebSocketClient.send({
			type: "RENAME_TOPIC",
			topic: topic.id,
			...edit
		});
	};

	const handleUpdateTopicDescription = (edit: UpdateAction) => {
		MeetingWebSocketClient.send({
			type: "DESCRIBE_TOPIC",
			topic: topic.id,
			...edit
		});
	};

	const handleUpdateTopicDuration = () => {
		MeetingWebSocketClient.send({
			type: "SCHEDULE_TOPIC",
			topic: topic.id,
			minutes: topic.duration
		});
	};

	const handleDeleteTopic = () => {
		MeetingWebSocketClient.send({
			type: "REMOVE_TOPIC",
			topic: topic.id
		});
	};
</script>

<div class="topic">
	<Input
		className="h2"
		bind:value={topic.name}
		placeholder={$t("common.untitled")}
		onAction={handleUpdateTopicName}
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
			onchange={handleUpdateTopicDuration}
		/>
		<span>{$t("common.minutesShort")}</span>
	</div>

	<TextArea
		bind:value={topic.description}
		placeholder={$t("common.startTyping")}
		onAction={handleUpdateTopicDescription}
	/>

	<div class="actions">
		<button
			class="handle"
			type="button"
			aria-label="Move topic"
			data-reorder-handle
		>
			<IconDrag />
		</button>
		<IconButton icon={IconDelete} onClick={handleDeleteTopic} />
	</div>
</div>

<style>
	.topic {
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
