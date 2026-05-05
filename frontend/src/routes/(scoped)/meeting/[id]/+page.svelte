<script lang="ts">
	import { onDestroy, onMount } from "svelte";

	import { page } from "$app/state";
	import { t } from "$lib/assets/translations";

	import IconPlus from "$lib/assets/icons/IconPlus.svelte";
	import Loading from "$lib/common/Loading.svelte";
	import type { UpdateAction } from "$lib/editor/ActionTypes";
	import Input from "$lib/editor/Input.svelte";
	import FeedbackButton from "$lib/form/FeedbackButton.svelte";
	import type {
		MeetingActionResponse,
		MeetingDetails,
		MeetingUpdateNameAction
	} from "$lib/meeting/MeetingTypes";
	import MeetingWebSocketClient from "$lib/meeting/MeetingWebSocketClient";
	import type { TopicUpdateNameAction } from "$lib/topic/TopicTypes";
	import TopicWebSocketClient from "$lib/topic/TopicWebSocketClient";

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

	const handleUpdateName = (action: UpdateAction) => {
		const request: MeetingUpdateNameAction = {
			action: "UPDATE_NAME",
			position: action.position,
			length: action.length,
			value: action.value
		};

		MeetingWebSocketClient.updateName(id, request);
	};

	const onEvent = (event: MeetingActionResponse) => {
		// TODO: filter out events caused by use. We should do this directly locally.
		if (event.type === "TOPIC_CREATE") {
			// TODO: what if initial data is not yet loaded?
			// TODO: keep in queue and apply once loaded.
			meeting?.topics.push({
				id: event.topicId,
				name: event.name
			});
		} else {
			// TODO: process
			console.log("Unhandled event:", event);
		}
	};

	function addTopic(): Promise<void> {
		const request = {
			name: ""
		};

		TopicWebSocketClient.create(id, request);
		return Promise.resolve();
	}

	const handleUpdateTopicName = (topicId: number, action: UpdateAction) => {
		const request: TopicUpdateNameAction = {
			action: "UPDATE_NAME",
			position: action.position,
			length: action.length,
			value: action.value
		};

		TopicWebSocketClient.updateName(id, topicId, request);
	};
</script>

<main class="container">
	{#if meeting}
		<!-- TODO: Move to Meeting view? -->
		<Input
			className="h1"
			bind:value={meeting.info.name}
			placeholder={$t("common.untitled")}
			onAction={handleUpdateName}
		/>

		<h2>{$t("common.topics")}</h2>
		<!-- TODO: regular button? Add ripple effect or some other feedback -->
		<FeedbackButton className="primary" onClick={addTopic}>
			<span class="label">
				<IconPlus />
				{$t("common.addObject", { object: $t("common.topic") })}
			</span>
		</FeedbackButton>

		<!-- // TODO: move to topic View -->
		{#each meeting.topics as topic}
			<Input
				className="h2"
				bind:value={topic.name}
				placeholder={$t("common.untitled")}
				onAction={action => handleUpdateTopicName(topic.id, action)}
			/>
		{/each}
	{:else}
		<Loading />
	{/if}
</main>
