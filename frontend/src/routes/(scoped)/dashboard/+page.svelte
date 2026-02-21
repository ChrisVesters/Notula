<script lang="ts">
	import { onDestroy, onMount } from "svelte";

	import { t } from "$lib/assets/translations";

	import IconPlus from "$lib/assets/icons/IconPlus.svelte";
	import FeedbackButton from "$lib/form/FeedbackButton.svelte";
	import MeetingClient from "$lib/meeting/MeetingClient";
	import type { MeetingInfo } from "$lib/meeting/MeetingTypes";

	import { Client, type IMessage, type Message } from "@stomp/stompjs";
	import Session from "$lib/auth/Session";
	import WebSocketManager from "$lib/websocket/WebSocketManager";

	let meetings: Array<MeetingInfo> = $state([]);

	onMount(async () => {
		meetings = await MeetingClient.getAll();

		WebSocketManager.connect();
		WebSocketManager.subscribe("/topic/meetings/4", onMessage);

	});

	onDestroy(() => {
		WebSocketManager.disconnect();
	});

	const onMessage = (event: Message) => {
		console.log(event.body);
	};

	function handleAddClick(): Promise<void> {
		return MeetingClient.create({ name: $t("common.untitled") })
			.then(meeting => {
				// TODO: open meeting note.
			})
			.catch(error => {
				// TODO: better error handling
				alert("Creating meeting failed. Please try again.");
			});
	}

	function sendMessage(): void {
		WebSocketManager.send("/app/meetings/4", "I am Groot!");
	}
</script>

<main class="container">
	<h1>Dashboard</h1>

	<FeedbackButton className="primary" onClick={handleAddClick}>
		<span class="label">
			<IconPlus />
			{$t("common.add")}
		</span>
	</FeedbackButton>

	{#each meetings as meeting}
		<div>{meeting.name}</div>
	{/each}

	<button onclick={sendMessage}>Send</button>
</main>
