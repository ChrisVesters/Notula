<script lang="ts">
	import { onDestroy, onMount } from "svelte";

	import { goto } from "$app/navigation";
	import { page } from "$app/state";

	import { BlockType } from "$lib/block/BlockTypes";
	import Loading from "$lib/common/Loading.svelte";
	import { isOwnEvent } from "$lib/common/EventTypes";
	import type {
		BlockDetails,
		MeetingDetails,
		TopicDetails
	} from "$lib/details/DetailTypes";
	import { applied } from "$lib/editor/TextEdit";
	import MeetingInfoView from "$lib/meeting/MeetingInfoView.svelte";
	import type { Rejected } from "$lib/meeting/change/ChangeTypes";
	import type { MeetingEvent } from "$lib/meeting/event/EventTypes";
	import MeetingWebSocketClient from "$lib/meeting/MeetingWebSocketClient";
	import TopicsAgendaView from "$lib/topic/TopicsAgendaView.svelte";
	import TopicsNoteView from "$lib/topic/TopicsNoteView.svelte";

	const id = $derived(Number(page.params.id));

	let meeting: MeetingDetails | undefined = $state();
	let topics = $derived(
		meeting?.topics?.toSorted((a, b) => a.sequenceId - b.sequenceId) ?? []
	);

	let revision: number | undefined = $state();
	// A change numbers every event it publishes with its own revision, so a
	// second event at the current revision belongs to the change being
	// applied and is not a repeat. A snapshot absorbs a change whole instead,
	// so an event still in flight for the revision it reports describes
	// something the payload already holds, and applying it would splice the
	// same edit in twice. This says which of the two we are on.
	let streamed = false;
	const buffered: Array<MeetingEvent> = [];

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
		revision = data.revision;
		streamed = false;

		buffered.splice(0, buffered.length).forEach(event => accept(event));
	};

	const onRejected = (rejected: Rejected) => {
		console.warn("Change refused:", rejected);
		window.alert(rejected.reason);
	};

	const onEvent = (event: MeetingEvent) => {
		console.debug("Received event:", event);

		accept(event);
	};

	const accept = (event: MeetingEvent) => {
		if (revision === undefined) {
			buffered.push(event);
			return;
		}

		const seen =
			event.revision < revision ||
			(event.revision === revision && !streamed);
		if (seen) {
			return;
		}

		if (event.revision > revision + 1) {
			resync();
			return;
		}

		revision = event.revision;
		streamed = true;

		mutate(event);
	};

	// Leaves the stale view up for the round trip rather than blanking the
	// page: events keep arriving and are replayed onto the snapshot, so the
	// divergence outlives the request by nothing.
	const resync = () => {
		if (revision === undefined) {
			return;
		}

		console.warn("Missed a change, reloading the meeting");
		revision = undefined;
		streamed = false;

		MeetingWebSocketClient.resync();
	};

	const findTopic = (id: number): TopicDetails | undefined => {
		const topic = meeting?.topics.find(t => t.id === id);
		if (!topic) {
			resync();
		}

		return topic;
	};

	const findBlock = (id: number): BlockDetails | undefined => {
		const block = meeting?.topics
			.flatMap(t => t.blocks)
			.find(b => b.id === id);
		if (!block) {
			resync();
		}

		return block;
	};

	const mutate = (event: MeetingEvent) => {
		// Our own changes come back too. Creating, moving and deleting topics
		// and blocks still relies on that echo to update the view, so they are
		// applied whoever caused them. Text is the exception: the editor
		// already applied it locally, and applying the echo would splice the
		// same edit in twice.
		const mutation = event.mutation;
		const remote = !isOwnEvent(event.origin);

		switch (mutation.type) {
			case "ADD_MEETING":
				break;
			case "RENAME_MEETING":
				if (meeting && remote) {
					meeting.name = applied(meeting.name, mutation);
				}
				break;
			case "DESCRIBE_MEETING":
				if (meeting && remote) {
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
					sequenceId: mutation.sequenceId,
					name: mutation.name,
					description: "",
					duration: null,
					blocks: []
				});
				break;
			case "MOVE_TOPIC": {
				const topic = findTopic(mutation.topic);
				if (topic) {
					topic.sequenceId = mutation.sequenceId;
				}
				break;
			}
			case "RENAME_TOPIC": {
				if (!remote) {
					break;
				}

				const topic = findTopic(mutation.topic);
				if (topic) {
					topic.name = applied(topic.name, mutation);
				}
				break;
			}
			case "DESCRIBE_TOPIC": {
				if (!remote) {
					break;
				}

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
						sequenceId: mutation.sequenceId,
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
					block.sequenceId = mutation.sequenceId;
				}
				break;
			}
			case "REMOVE_BLOCK": {
				const topic = meeting?.topics.find(t =>
					t.blocks.some(b => b.id === mutation.block)
				);
				if (!topic) {
					resync();
					break;
				}

				topic.blocks = topic.blocks.filter(
					b => b.id !== mutation.block
				);
				break;
			}

			case "EDIT_TEXT_BLOCK": {
				if (!remote) {
					break;
				}

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
