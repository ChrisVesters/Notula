<script lang="ts">
	import { t } from "$lib/assets/translations";

	import IconDelete from "$lib/assets/icons/IconDelete.svelte";
	import IconDrag from "$lib/assets/icons/IconDrag.svelte";

	import type { TopicDetails } from "$lib/details/DetailTypes";
	import type { UpdateAction } from "$lib/editor/ActionTypes";
	import Input from "$lib/editor/Input.svelte";
	import TextArea from "$lib/editor/TextArea.svelte";
	import IconButton from "$lib/form/IconButton.svelte";

	import {
		topicDrag,
		type DragSource,
		type DropPosition
	} from "./TopicDrag.svelte";
	import type {
		TopicDeleteAction,
		TopicMoveAction,
		TopicUpdateDescriptionAction,
		TopicUpdateDurationAction,
		TopicUpdateNameAction
	} from "./TopicTypes";
	import TopicWebSocketClient from "./TopicWebSocketClient";

	export type TopicAgendaViewProps = {
		topic: Readonly<TopicDetails>;
		meetingId: Readonly<number>;
	};

	let { topic = $bindable(), meetingId }: TopicAgendaViewProps = $props();

	let draggable = $state(false);

	const dragged = $derived(topicDrag.source?.topicId === topic.id);
	const dropPosition = $derived(
		topicDrag.target?.topicId === topic.id
			? topicDrag.target.position
			: null
	);

	const handleDragStart = (event: DragEvent) => {
		if (!event.dataTransfer) {
			return;
		}

		event.dataTransfer.effectAllowed = "move";
		// Firefox only starts a drag once data has been set.
		event.dataTransfer.setData("text/plain", "");

		topicDrag.start({
			meetingId,
			topicId: topic.id,
			sequenceId: topic.sequenceId
		});
	};

	const handleDragEnd = () => {
		draggable = false;
		topicDrag.end();
	};

	const handleDragOver = (
		event: DragEvent & { currentTarget: HTMLElement }
	) => {
		if (!event.dataTransfer || topicDrag.source?.meetingId !== meetingId) {
			return;
		}

		event.preventDefault();
		event.dataTransfer.dropEffect = "move";

		const bounds = event.currentTarget.getBoundingClientRect();
		const middle = bounds.top + bounds.height / 2;

		topicDrag.over({
			topicId: topic.id,
			position: event.clientY < middle ? "BEFORE" : "AFTER"
		});
	};

	const handleDrop = (event: DragEvent) => {
		const source = topicDrag.source;
		const position = dropPosition;

		topicDrag.end();

		if (!source || position === null) {
			return;
		}

		event.preventDefault();

		handleMoveTopic(source, position);
	};

	const handleMoveTopic = (source: DragSource, position: DropPosition) => {
		// The index of the gap the topic is dropped in, before removal.
		const gap =
			position === "BEFORE" ? topic.sequenceId : topic.sequenceId + 1;
		const sequenceId = gap > source.sequenceId ? gap - 1 : gap;

		if (sequenceId === source.sequenceId) {
			return;
		}

		const request: TopicMoveAction = {
			topicId: source.topicId,
			sequenceId
		};

		TopicWebSocketClient.move(request);
	};

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

	const handleUpdateTopicDescription = (action: UpdateAction) => {
		const request: TopicUpdateDescriptionAction = {
			topicId: topic.id,
			action: "UPDATE_DESCRIPTION",
			position: action.position,
			length: action.length,
			value: action.value
		};

		TopicWebSocketClient.update(request);
	};

	const handleUpdateTopicDuration = () => {
		const request: TopicUpdateDurationAction = {
			topicId: topic.id,
			action: "UPDATE_DURATION",
			duration: topic.duration
		};

		TopicWebSocketClient.update(request);
	};

	const handleDeleteTopic = () => {
		const request: TopicDeleteAction = {
			topicId: topic.id
		};

		TopicWebSocketClient.delete(request);
	};
</script>

<li
	class="topic"
	class:dragged
	class:drop-before={dropPosition === "BEFORE"}
	class:drop-after={dropPosition === "AFTER"}
	{draggable}
	ondragstart={handleDragStart}
	ondragend={handleDragEnd}
	ondragover={handleDragOver}
	ondrop={handleDrop}
>
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
			onpointerdown={() => (draggable = true)}
			onpointerup={() => (draggable = false)}
		>
			<IconDrag />
		</button>
		<IconButton
			icon={IconDelete}
			onClick={handleDeleteTopic}
		/>
	</div>
</li>

<style>
	.topic {
		position: relative;
		margin-top: 1rem;
	}

	.topic.dragged {
		opacity: 0.4;
	}

	.topic.drop-before::before,
	.topic.drop-after::after {
		content: "";

		position: absolute;
		left: 0;
		right: 0;

		border-top: 2px solid var(--color-primary-500);
	}

	.topic.drop-before::before {
		top: -0.5rem;
	}

	.topic.drop-after::after {
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
