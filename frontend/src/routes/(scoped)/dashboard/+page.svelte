<script lang="ts">
	import { onMount } from "svelte";

	import { goto } from "$app/navigation";
	import { t } from "$lib/assets/translations";

	import IconPlus from "$lib/assets/icons/IconPlus.svelte";
	import FeedbackButton from "$lib/form/FeedbackButton.svelte";
	import MeetingClient from "$lib/meeting/MeetingClient";
	import type { MeetingInfo } from "$lib/meeting/MeetingTypes";

	let meetings: Array<MeetingInfo> = $state([]);

	onMount(async () => {
		meetings = await MeetingClient.getAll();
	});

	function addMeeting(): Promise<void> {
		return MeetingClient.create({ name: $t("common.untitled") })
			.then(meeting => {
				// TODO: open meeting note.
			})
			.catch(error => {
				// TODO: better error handling
				alert("Creating meeting failed. Please try again.");
			});
	}

	function openMeeting(id: number): void {
		goto(`/meeting/${id}`);
	}
</script>

<main class="container">
	<h1>{$t("common.dashboard")}</h1>

	<FeedbackButton className="primary" onClick={addMeeting}>
		<span class="label">
			<IconPlus />
			{$t("common.add")}
		</span>
	</FeedbackButton>

	<ul>
		{#each meetings as meeting}
			<li>
				<a href={`/meeting/${meeting.id}`}>{meeting.name}</a>
			</li>
		{/each}
	</ul>
</main>
