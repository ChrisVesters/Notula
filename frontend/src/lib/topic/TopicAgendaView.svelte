<script lang="ts">
	import { t } from "$lib/assets/translations";

	import IconDelete from "$lib/assets/icons/IconDelete.svelte";
	import IconDrag from "$lib/assets/icons/IconDrag.svelte";

	import { DropPosition, reorderHandler } from "$lib/common/ReorderHandler";
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

	let dragged = $state(false);
	let dropPosition: DropPosition | null = $state(null);

	const handleMoveTopic = (sequenceId: number) => {
		MeetingWebSocketClient.send({
			type: "MOVE_TOPIC",
			topic: topic.id,
			sequenceId
		});
	};

	const handleReorder = $derived(
		reorderHandler({
			sequenceId: topic.sequenceId,
			onDragChange: (value: boolean) => (dragged = value),
			onDropChange: (value: DropPosition | null) =>
				(dropPosition = value),
			onMove: handleMoveTopic
		})
	);

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

<li
	class="topic"
	class:dragged
	class:drop-before={dropPosition === DropPosition.BEFORE}
	class:drop-after={dropPosition === DropPosition.AFTER}
	{@attach handleReorder}
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
			data-reorder-handle
		>
			<IconDrag />
		</button>
		<IconButton icon={IconDelete} onClick={handleDeleteTopic} />
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
