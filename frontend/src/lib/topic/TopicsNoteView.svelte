<script lang="ts">
	import { t } from "$lib/assets/translations";

	import type { TopicDetails } from "$lib/details/DetailTypes";

	import TopicNoteView from "./TopicNoteView.svelte";

	export type TopicsNoteViewProps = {
		topics: Readonly<Array<TopicDetails>>;
	};

	let { topics = $bindable() }: TopicsNoteViewProps = $props();
</script>

<h2>{$t("common.notes")}</h2>
{#each topics as topic, index (topic.id)}
	<TopicNoteView bind:topic={topics[index]} />
{/each}
