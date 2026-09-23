<script lang="ts">
	import { t } from "$lib/assets/translations";

	import IconPlus from "$lib/assets/icons/IconPlus.svelte";

	import ReorderList from "$lib/common/ReorderList.svelte";
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
			afterId: topics.at(-1)?.id ?? null,
			name: ""
		});

		return Promise.resolve();
	}

	const handleMoveTopic = (topicId: number, afterId: number | null) => {
		MeetingWebSocketClient.send({
			type: "MOVE_TOPIC",
			topic: topicId,
			afterId
		});
	};
</script>

<h2>{$t("common.agenda")}</h2>
<!-- TODO: regular button? Add ripple effect or some other feedback -->
<FeedbackButton className="primary" onClick={addTopic}>
	<span class="label">
		<IconPlus />
		{$t("common.addObject", { object: $t("common.topic") })}
	</span>
</FeedbackButton>

<ReorderList items={topics} onMove={handleMoveTopic}>
	{#snippet item(_: TopicDetails, index: number)}
		<TopicAgendaView bind:topic={topics[index]} />
	{/snippet}
</ReorderList>
