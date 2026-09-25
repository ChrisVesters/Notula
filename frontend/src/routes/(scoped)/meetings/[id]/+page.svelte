<script lang="ts">
	import { onDestroy, onMount } from "svelte";

	import { goto } from "$app/navigation";
	import { page } from "$app/state";

	import { BlockType } from "$lib/block/BlockTypes";
	import Loading from "$lib/common/Loading.svelte";
	import { Rank } from "$lib/common/Rank";
	import type {
		BlockDetails,
		MeetingDetails,
		TopicDetails
	} from "$lib/details/DetailTypes";
	import { applied } from "$lib/editor/TextEdit";
	import type { Rejected } from "$lib/meeting/change/ChangeTypes";
	import type { MeetingEvent } from "$lib/meeting/event/EventTypes";
	import MeetingInfoView from "$lib/meeting/MeetingInfoView.svelte";
	import MeetingWebSocketClient from "$lib/meeting/MeetingWebSocketClient";
	import TopicsAgendaView from "$lib/topic/TopicsAgendaView.svelte";
	import TopicsNoteView from "$lib/topic/TopicsNoteView.svelte";

	const id = $derived(Number(page.params.id));

	let meeting: MeetingDetails | undefined = $state();
	let topics = $derived(
		meeting?.topics?.toSorted((a, b) => Rank.compare(a.rank, b.rank)) ?? []
	);

	onMount(async () => {
		MeetingWebSocketClient.connect(id, {
			onLoad,
			onEvent,
			onRejected
		});
	});

	onDestroy(() => {
		MeetingWebSocketClient.disconnect();
	});

	const onLoad = (data: MeetingDetails) => {
		meeting = data;
	};

	const onRejected = (rejected: Rejected) => {
		console.warn("Change refused:", rejected);
		window.alert(rejected.reason);
	};

	const onEvent = (event: MeetingEvent) => {
		console.debug("Received event:", event);

		mutate(event);
	};

	const findTopic = (id: number): TopicDetails | undefined => {
		const topic = meeting?.topics.find(t => t.id === id);
		if (!topic) {
			MeetingWebSocketClient.resync();
		}

		return topic;
	};

	const findBlock = (id: number): BlockDetails | undefined => {
		const block = meeting?.topics
			.flatMap(t => t.blocks)
			.find(b => b.id === id);
		if (!block) {
			MeetingWebSocketClient.resync();
		}

		return block;
	};

	const mutate = (event: MeetingEvent) => {
		const mutation = event.mutation;

		switch (mutation.type) {
			case "ADD_MEETING":
				break;
			case "RENAME_MEETING":
				if (meeting) {
					meeting.name = applied(meeting.name, mutation);
				}
				break;
			case "DESCRIBE_MEETING":
				if (meeting) {
					meeting.description = applied(
						meeting.description,
						mutation
					);
				}
				break;
			case "REMOVE_MEETING":
				// TODO: show message saying the meeting was deleted/no longer exists.
				goto("/meetings");
				break;

			case "ADD_TOPIC":
				meeting?.topics.push({
					id: mutation.topic,
					rank: mutation.rank,
					name: mutation.name,
					description: "",
					duration: null,
					blocks: []
				});
				break;
			case "MOVE_TOPIC": {
				const topic = findTopic(mutation.topic);
				if (topic) {
					topic.rank = mutation.rank;
				}
				break;
			}
			case "RENAME_TOPIC": {
				const topic = findTopic(mutation.topic);
				if (topic) {
					topic.name = applied(topic.name, mutation);
				}
				break;
			}
			case "DESCRIBE_TOPIC": {
				const topic = findTopic(mutation.topic);
				if (topic) {
					topic.description = applied(topic.description, mutation);
				}
				break;
			}
			case "SCHEDULE_TOPIC": {
				const topic = findTopic(mutation.topic);
				if (topic) {
					topic.duration = mutation.minutes;
				}
				break;
			}
			case "REMOVE_TOPIC": {
				const index =
					meeting?.topics.findIndex(t => t.id === mutation.topic) ??
					-1;
				if (index >= 0) {
					meeting?.topics.splice(index, 1);
				}
				break;
			}

			case "ADD_BLOCK": {
				const topic = findTopic(mutation.topic);
				if (!topic) {
					break;
				}

				if (mutation.blockType === BlockType.TEXT) {
					topic.blocks.push({
						id: mutation.block,
						type: mutation.blockType,
						rank: mutation.rank,
						content: ""
					});
				} else {
					console.error("Unhandled block type:", mutation.blockType);
				}
				break;
			}
			case "MOVE_BLOCK": {
				const block = findBlock(mutation.block);
				if (block) {
					block.rank = mutation.rank;
				}
				break;
			}
			case "REMOVE_BLOCK": {
				const topic = meeting?.topics.find(t =>
					t.blocks.some(b => b.id === mutation.block)
				);
				if (!topic) {
					MeetingWebSocketClient.resync();
					break;
				}

				topic.blocks = topic.blocks.filter(
					b => b.id !== mutation.block
				);
				break;
			}

			case "EDIT_TEXT_BLOCK": {
				const block = findBlock(mutation.block);
				if (block?.type === BlockType.TEXT) {
					block.content = applied(block.content, mutation);
				}
				break;
			}
		}
	};
</script>

{#if meeting}
	<MeetingInfoView bind:meeting />

	<TopicsAgendaView bind:topics />
	<TopicsNoteView bind:topics />
{:else}
	<Loading />
{/if}
