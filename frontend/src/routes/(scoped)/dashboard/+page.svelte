<script lang="ts">
	import { onMount } from "svelte";

	import { t } from "$lib/assets/translations";

	import IconPlus from "$lib/assets/icons/IconPlus.svelte";
	import FeedbackButton from "$lib/form/FeedbackButton.svelte";
	import MeetingClient from "$lib/meeting/MeetingClient";
	import type { MeetingInfo } from "$lib/meeting/MeetingTypes";

	import { Client } from "@stomp/stompjs";
	import Session from "$lib/auth/Session";

	let meetings: Array<MeetingInfo> = $state([]);

	onMount(async () => {
		meetings = await MeetingClient.getAll();

		// Connecting websocket
		const client = new Client({
			brokerURL: import.meta.env.VITE_WS_URL,
			connectHeaders: {
				Authorization: "Bearer " + Session.getAccessToken()
			},
			onConnect: () => {
				console.log("Connected...");
				client.subscribe("/topic/messages", onMessage);
				client.publish({destination: "/app/chat", body: "Hello World"});
			}
		});

		client.activate();
		console.log("Connecting...");
	});

	const onMessage = (event: any) => {
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
</main>
