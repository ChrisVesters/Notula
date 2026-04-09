<script lang="ts">
	import { onDestroy, onMount } from "svelte";

	import { page } from "$app/state";
	import { t } from "$lib/assets/translations";

	import IconPlus from "$lib/assets/icons/IconPlus.svelte";
	import Loading from "$lib/common/Loading.svelte";
	import FeedbackButton from "$lib/form/FeedbackButton.svelte";
	import type {
		MeetingActionResponse,
		MeetingDetails
	} from "$lib/meeting/MeetingTypes";
	import MeetingWebSocketClient from "$lib/meeting/MeetingWebSocketClient";
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

	const onEvent = (event: MeetingActionResponse) => {
		if (event.type === "TOPIC_CREATE") {
			// TODO: what if initial data is not yet loaded?
			// TODO: keep in queue and apply once loaded.
			meeting?.topics.push({
				id: event.id,
				name: event.name
			});
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
		<h1>{meeting.info.name}</h1>
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
