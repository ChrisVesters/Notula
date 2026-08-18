<script lang="ts">
	import { t } from "$lib/assets/translations";

	import type { TopicDetails } from "$lib/details/DetailTypes";

	import TopicNoteView from "./TopicNoteView.svelte";

	export type TopicsNoteViewProps = {
		meetingId: Readonly<number>;
		topics: Readonly<Array<TopicDetails>>;
	};

	let { meetingId, topics = $bindable() }: TopicsNoteViewProps = $props();
</script>

<h2>{$t("common.notes")}</h2>
{#each topics as topic, index (topic.id)}
	<TopicNoteView {meetingId} bind:topic={topics[index]} />
{/each}
