<script lang="ts">
	import { onMount } from "svelte";

	import { goto } from "$app/navigation";
	import { t } from "$lib/assets/translations";

	import IconPlus from "$lib/assets/icons/IconPlus.svelte";

	import { trim } from "$lib/common/NameUtils";

	import FeedbackButton from "$lib/form/FeedbackButton.svelte";
	import MeetingClient from "$lib/meeting/MeetingClient";
	import type { MeetingInfo } from "$lib/meeting/MeetingTypes";
	import IconDelete from "$lib/assets/icons/IconDelete.svelte";
	import IconButton from "$lib/form/IconButton.svelte";

	let meetings: Array<MeetingInfo> = $state([]);

	onMount(async () => {
		meetings = await MeetingClient.getAll();
	});

	const addMeeting = async (): Promise<void> => {
		try {
			const meeting = await MeetingClient.create({ name: "" });
			goto(`/meetings/${meeting.id}`);
		} catch (error) {
			// TODO: better error handling
			alert("Creating meeting failed. Please try again.");
		}
	}

	const deleteMeeting = async (meetingId: number): Promise<void> => {
		try {
			await MeetingClient.delete(meetingId);
			meetings = meetings.filter(meeting => meeting.id !== meetingId);
		} catch (error) {
			// TODO: better error handling
			alert("Deleting meeting failed. Please try again.");
		}
	}
</script>

<h1>{$t("common.meetings")}</h1>

<FeedbackButton className="primary" onClick={addMeeting}>
	<span class="label">
		<IconPlus />
		{$t("common.add")}
	</span>
</FeedbackButton>

<ul>
	{#each meetings as meeting}
		<li>
			<a href={`/meetings/${meeting.id}`}>
				{trim(meeting.name, $t("common.untitled"))}
			</a>
			<IconButton
				icon={IconDelete}
				onClick={() => deleteMeeting(meeting.id)}
			/>
		</li>
	{/each}
</ul>
