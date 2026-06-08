<script lang="ts">
	import { onDestroy, onMount } from "svelte";

	import { page } from "$app/state";
	import { t } from "$lib/assets/translations";

	import IconPlus from "$lib/assets/icons/IconPlus.svelte";
	import type { BlockMutation } from "$lib/block/BlockTypes";
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
	import type { MeetingMessage } from "$lib/meeting/MeetingTypes";
	import MeetingWebSocketClient from "$lib/meeting/MeetingWebSocketClient";
	import TopicAgendaView from "$lib/topic/TopicAgendaView.svelte";
	import type {
		TopicMutation,
		TopicUpdateNameAction
	} from "$lib/topic/TopicTypes";
	import TopicWebSocketClient from "$lib/topic/TopicWebSocketClient";
	import BlockView from "$lib/block/BlockView.svelte";

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

	const onEvent = (event: MeetingMessage) => {
		// TODO: filter out events caused by use. We should do this directly locally.
		// TODO: what if initial data is not yet loaded?
		// TODO: keep in queue and apply once loaded.
		// TODO: swich case?
		if (event.target == "TOPIC") {
			const mutation: TopicMutation = event.mutation;
			if (mutation.action === "CREATE") {
				meeting?.topics.push({
					id: event.topicId,
					name: mutation.name,
					blocks: []
				});
			}
		} else if (event.target === "BLOCK") {
			const mutation: BlockMutation = event.mutation;
			if (mutation.action === "CREATE") {
				const topic = meeting?.topics.find(t => t.id === event.topicId);
				// TODO: what if topic does not exist? Out of sync?
				if (!topic) {
					console.error("Topic does not exist");
					return;
				}

				if (mutation.type === BlockType.TEXT) {
					topic.blocks.push({
						id: event.blockId,
						type: mutation.type,
						sequenceId: mutation.sequenceId,
						content: ""
					});
				} else {
					console.error("Unhandled block type:", mutation.type);
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
		{#each topic.blocks as block, index (block.id)}
			<BlockView
				meetingId={id}
				topicId={topic.id}
				bind:block={topic.blocks[index]}
			/>
		{/each}
	{/each}
{:else}
	<Loading />
{/if}
