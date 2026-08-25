<script lang="ts">
	import { t } from "$lib/assets/translations";

	import IconPlus from "$lib/assets/icons/IconPlus.svelte";

	import type { TopicDetails } from "$lib/details/DetailTypes";
	import FeedbackButton from "$lib/form/FeedbackButton.svelte";

	import TopicAgendaView from "./TopicAgendaView.svelte";
	import type {
		TopicCreateAction
	} from "./TopicTypes";
	import TopicWebSocketClient from "./TopicWebSocketClient";

	export type TopicsAgendaViewProps = {
		meetingId: Readonly<number>;
		topics: Readonly<Array<TopicDetails>>;
	};

	let { meetingId, topics = $bindable() }: TopicsAgendaViewProps = $props();

	function addTopic(): Promise<void> {
		const request: TopicCreateAction = {
			meetingId,
			sequenceId: topics.length,
			name: ""
		};

		TopicWebSocketClient.create(request);
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
		<TopicAgendaView bind:topic={topics[index]} {meetingId} />
	{/each}
</ul>

<style>
	.topics {
		list-style: none;
		margin: 0;
		padding: 0;
	}
</style>
