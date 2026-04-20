<script lang="ts">
	import { onDestroy, onMount } from "svelte";

	import { page } from "$app/state";
	import { t } from "$lib/assets/translations";

	import IconPlus from "$lib/assets/icons/IconPlus.svelte";
	import Loading from "$lib/common/Loading.svelte";
	import FeedbackButton from "$lib/form/FeedbackButton.svelte";
	import type {
		MeetingActionResponse,
		MeetingDetails,
		MeetingUpdateNameAction
	} from "$lib/meeting/MeetingTypes";
	import MeetingWebSocketClient from "$lib/meeting/MeetingWebSocketClient";
	import TopicWebSocketClient from "$lib/topic/TopicWebSocketClient";
	import Input from "$lib/editor/Input.svelte";
	import type { UpdateAction } from "$lib/editor/ActionTypes";

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
				id: event.id,
				name: event.name
			});
		} else {
			// TODO: process
			console.log("Unhandled event:", event);
		}
	};

	function addTopic(): Promise<void> {
		const request = {
			name: $t("common.untitled")
		};

		TopicWebSocketClient.create(id, request);
		return Promise.resolve();
	}
</script>

<main class="container">
	{#if meeting}
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

		{#each meeting.topics as topic}
			<h3>{topic.name}</h3>
		{/each}
	{:else}
		<Loading />
	{/if}
</main>
