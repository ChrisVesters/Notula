<script lang="ts">
	import { t } from "$lib/assets/translations";

	import IconPlus from "$lib/assets/icons/IconPlus.svelte";

	import type { TopicDetails } from "$lib/details/DetailTypes";
	import FeedbackButton from "$lib/form/FeedbackButton.svelte";

	import TopicAgendaView from "./TopicAgendaView.svelte";
	import MeetingWebSocketClient from "$lib/meeting/MeetingWebSocketClient";

	export type TopicsAgendaViewProps = {
		topics: Readonly<Array<TopicDetails>>;
	};

	let { topics = $bindable() }: TopicsAgendaViewProps = $props();

	function addTopic(): Promise<void> {
		MeetingWebSocketClient.send({
			type: "ADD_TOPIC",
			sequenceId: topics.length,
			name: ""
		});

		return Promise.resolve();
	}
</script>

<h2>{$t("common.agenda")}</h2>
<!-- TODO: regular button? Add ripple effect or some other feedback -->
<FeedbackButton className="primary" onClick={addTopic}>
	<span class="label">
		<IconPlus />
		{$t("common.addObject", { object: $t("common.topic") })}
	</span>
</FeedbackButton>

<ul class="topics">
	{#each topics as topic, index (topic.id)}
		<TopicAgendaView bind:topic={topics[index]} />
	{/each}
</ul>

<style>
	.topics {
		list-style: none;
		margin: 0;
		padding: 0;
	}
</style>
