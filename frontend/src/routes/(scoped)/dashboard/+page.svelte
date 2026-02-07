<script lang="ts">
	import { onMount } from "svelte";

	import { t } from "$lib/assets/translations";

	import IconPlus from "$lib/assets/icons/IconPlus.svelte";
	import FeedbackButton from "$lib/form/FeedbackButton.svelte";
	import MeetingClient from "$lib/meeting/MeetingClient";
	import type { MeetingInfo } from "$lib/meeting/MeetingTypes";
	import SessionClient from "$lib/session/SessionClient";
	import DataStorage from "$lib/common/DataStorage";
	import Auth from "$lib/auth/Auth";

	let meetings: Array<MeetingInfo> = $state([]);

	onMount(async () => {
		meetings = await MeetingClient.getAll();
	});

	function handleAddClick(): Promise<void> {
		return MeetingClient.create({ name: $t("untitled") })
			.then(meeting => {
				// TODO: open meeting note.
			})
			.catch(error => {
				// TODO: better error handling
				alert("Creating meeting failed. Please try again.");
			});
	}

	function refresh(): void {
		const sessionId = Number(DataStorage.getItem("sessionId"));

		SessionClient.refresh(sessionId).then(session => {
				Auth.updatePrincipal(session.accessToken);
			})
	}
</script>

<main class="container">
	<h1>Dashboard</h1>

	<FeedbackButton className="primary" onClick={handleAddClick}>
		<span class="label">
			<IconPlus />
			{$t("add")}
		</span>
	</FeedbackButton>

	{#each meetings as meeting}
		<div>{meeting.name}</div>
	{/each}

	<button onclick={refresh}>
		Refresh
	</button>
</main>
