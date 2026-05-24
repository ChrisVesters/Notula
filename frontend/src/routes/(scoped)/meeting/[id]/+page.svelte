<script lang="ts">
	import { onDestroy, onMount } from "svelte";

	import { page } from "$app/state";
	import { t } from "$lib/assets/translations";

	import IconPlus from "$lib/assets/icons/IconPlus.svelte";
	import { BlockType } from "$lib/block/BlockTypes";
	import BlockWebSocketClient from "$lib/block/BlockWebSocketClient";
	import Loading from "$lib/common/Loading.svelte";
	import type {
		MeetingDetails,
		TopicDetails
	} from "$lib/details/DetailTypes";
	import type { UpdateAction } from "$lib/editor/ActionTypes";
	import Input from "$lib/editor/Input.svelte";
	import FeedbackButton from "$lib/form/FeedbackButton.svelte";
	import MeetingInfoView from "$lib/meeting/MeetingInfoView.svelte";
	import type { MeetingActionResponse } from "$lib/meeting/MeetingTypes";
	import MeetingWebSocketClient from "$lib/meeting/MeetingWebSocketClient";
	import type { TopicUpdateNameAction } from "$lib/topic/TopicTypes";
	import TopicWebSocketClient from "$lib/topic/TopicWebSocketClient";
	import TopicAgendaView from "$lib/topic/TopicAgendaView.svelte";

	const id = $derived(Number(page.params.id));

	let meeting: MeetingDetails | null = $state(null);

	onMount(async () => {
		MeetingWebSocketClient.connect(id, {
			onLoad,
			onError,
			onEvent
		});
	});

	onDestroy(() => {
		MeetingWebSocketClient.disconnect(id);
	});

	const onLoad = (data: MeetingDetails) => {
		meeting = data;
	};

	const onError = (message: string) => {
		window.alert(message);
	};

	const onEvent = (event: MeetingActionResponse) => {
		// TODO: filter out events caused by use. We should do this directly locally.
		// TODO: always use target
		if ("type" in event && event.type === "TOPIC_CREATE") {
			// TODO: what if initial data is not yet loaded?
			// TODO: keep in queue and apply once loaded.
			meeting?.topics.push({
				id: event.topicId,
				name: event.name,
				blocks: []
			});
		} else if ("target" in event && event.target === "BLOCK") {
			if (event.mutation.action === "CREATE") {
				const topic = meeting?.topics.find(t => t.id === event.topicId);
				if (topic) {
					topic.blocks.push({
						id: event.blockId,
						type: event.mutation.type,
						sequenceId: event.mutation.sequenceId
					});
				}
			}
		} else {
			// TODO: process
			console.log("Unhandled event:", event);
		}
	};

	const handleUpdateTopicName = (topicId: number, action: UpdateAction) => {
		const request: TopicUpdateNameAction = {
			action: "UPDATE_NAME",
			position: action.position,
			length: action.length,
			value: action.value
		};

		TopicWebSocketClient.updateName(id, topicId, request);
	};

	function addBlock(topic: TopicDetails): Promise<void> {
		const request = {
			type: BlockType.TEXT,
			sequenceId: topic.blocks.length
		};

		const topicId = topic.id;
		BlockWebSocketClient.create(id, topicId, request);

		return Promise.resolve();
	}
</script>

<main class="container">
	{#if meeting}
		<MeetingInfoView bind:meeting />

		<TopicAgendaView meetingId={meeting.id} bind:topics={meeting.topics} />

		<h2>{$t("common.notes")}</h2>
		{#each meeting.topics as topic (topic.id)}
			<Input
				className="h2"
				bind:value={topic.name}
				placeholder={$t("common.untitled")}
				onAction={action => handleUpdateTopicName(topic.id, action)}
			/>
			<FeedbackButton className="primary" onClick={() => addBlock(topic)}>
				<span class="label">
					<IconPlus />
					{$t("common.addObject", { object: $t("common.note") })}
				</span>
			</FeedbackButton>

			<!-- TODO: Sort -->
			<!-- $: sortedItems = items
  .slice()
  .sort((a, b) => a.id - b.id); -->
			{#each topic.blocks as block (block.id)}
				<div>{block.type}</div>
			{/each}
		{/each}
	{:else}
		<Loading />
	{/if}
</main>
