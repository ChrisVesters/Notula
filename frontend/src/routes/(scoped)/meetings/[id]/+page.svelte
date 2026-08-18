<script lang="ts">
	import { onDestroy, onMount } from "svelte";

	import { goto } from "$app/navigation";
	import { page } from "$app/state";

	import type { BlockMutation } from "$lib/block/BlockTypes";
	import { BlockType } from "$lib/block/BlockTypes";
	import Loading from "$lib/common/Loading.svelte";
	import type {
		MeetingDetails
	} from "$lib/details/DetailTypes";
	import MeetingInfoView from "$lib/meeting/MeetingInfoView.svelte";
	import type {
		MeetingMessage,
		MeetingMutation
	} from "$lib/meeting/MeetingTypes";
	import MeetingWebSocketClient from "$lib/meeting/MeetingWebSocketClient";
	import TopicsAgendaView from "$lib/topic/TopicsAgendaView.svelte";
	import TopicsNoteView from "$lib/topic/TopicsNoteView.svelte";
	import type {
		TopicMutation
	} from "$lib/topic/TopicTypes";

	const id = $derived(Number(page.params.id));

	let meeting: MeetingDetails | undefined = $state();
	let topics = $derived(meeting?.topics?.toSorted((a, b) => a.sequenceId - b.sequenceId) ?? []);

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
		// TODO: extract this logic into handlers? Or at least separate functions
		console.debug("Received event:", event);
		if (event.target == "MEETING") {
			const mutation: MeetingMutation = event.mutation;
			if (mutation.action === "DELETE") {
				// TODO: show message saying the meeting was deleted/no longer exists.
				goto("/meetings");
			}
		} else if (event.target == "TOPIC") {
			const mutation: TopicMutation = event.mutation;
			if (mutation.action === "CREATE") {
				meeting?.topics.push({
					id: event.topicId,
					sequenceId: mutation.sequenceId,
					name: mutation.name,
					description: "",
					duration: null,
					blocks: []
				});
			} else if (mutation.action === "UPDATE_DURATION") {
				const topic = meeting?.topics.find(t => t.id === event.topicId);
				if (topic) {
					topic.duration = mutation.duration;
				}
			} else if (mutation.action === "DELETE") {
				const index = meeting?.topics.findIndex(t => t.id === event.topicId);
				if (index !== undefined && index >= 0) {
					meeting?.topics.splice(index, 1);
				}
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
			} else if (mutation.action === "DELETE") {
				const topic = meeting?.topics.find(t => t.id === event.topicId);
				// TODO: what if topic does not exist? Out of sync?
				if (!topic) {
					console.error("Topic does not exist");
					return;
				}

				const index = topic.blocks.findIndex(b => b.id === event.blockId);
				if (index !== undefined && index >= 0) {
					topic.blocks.splice(index, 1);
				}
			}
		}
	};



</script>

{#if meeting}
	<MeetingInfoView bind:meeting />

	<TopicsAgendaView meetingId={meeting.id} bind:topics={topics} />
	<TopicsNoteView meetingId={meeting.id} bind:topics={topics} />
{:else}
	<Loading />
{/if}
