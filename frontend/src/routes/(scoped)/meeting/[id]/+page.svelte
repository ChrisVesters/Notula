<script lang="ts">
	import { onDestroy, onMount } from "svelte";

	import { page } from "$app/state";
	import { t } from "$lib/assets/translations";

	import type { Message } from "@stomp/stompjs";

	import IconPlus from "$lib/assets/icons/IconPlus.svelte";
	import Loading from "$lib/common/Loading.svelte";
	import FeedbackButton from "$lib/form/FeedbackButton.svelte";
	import type { MeetingDetails } from "$lib/meeting/MeetingTypes";
	import WebSocketManager from "$lib/websocket/WebSocketManager";

	const id = $derived(page.params.id);

	let meeting: MeetingDetails | null = $state(null);

	onMount(async () => {
		// TODO: connect as soon as organisation is selected
		WebSocketManager.connect();

		WebSocketManager.subscribe(`/topic/meetings/${id}`, onMessage);
		WebSocketManager.subscribe(`/app/meetings/${id}`, onInitialData);
	});

	onDestroy(() => {
		WebSocketManager.disconnect();
	});

	const onInitialData = (event: Message) => {
		meeting = JSON.parse(event.body);
	};

	const onMessage = (event: Message) => {
		console.log(event.body);
	};

	function sendMessage(): void {
		WebSocketManager.send(`/app/meetings/${id}`, "I am Groot!");
	}

	function addTopic(): Promise<void> {
		// TODO: proper objects
		return WebSocketManager.send(`/app/meetings/${id}`, "New topic");
	}
</script>

<main class="container">
	{#if meeting}
		<h1>{meeting.name}</h1>
		<h2>{$t("common.topics")}</h2>
		<FeedbackButton className="primary" onClick={addTopic}>
			<span class="label">
				<IconPlus />
				{$t("common.addObject", { object: $t("common.topic") })}
			</span>
		</FeedbackButton>

		<button onclick={sendMessage}>Send</button>
	{:else}
		<Loading />
	{/if}
</main>
