<script lang="ts">
	import { t } from "$lib/assets/translations";
	import FeedbackButton from "$lib/form/FeedbackButton.svelte";

	import TextField from "$lib/form/TextField.svelte";

	import type { OrganisationInfo } from "./OrganisationTypes";

	export type OrganisationInfoViewProps = {
		organisation: OrganisationInfo;

		onSave: () => Promise<void>;
		onCancel: () => void;
	};

	const { organisation, onSave, onCancel }: OrganisationInfoViewProps =
		$props();
</script>

<TextField
	bind:value={organisation.name}
	label={$t("common.name")}
	id="name"
	type="text"
	required
	autocomplete="organization"
/>

<button class="btn secondary" onclick={onCancel}>{$t("common.cancel")}</button>
<FeedbackButton className="primary" onClick={onSave}>
	<span class="label">{$t("common.save")}</span>
</FeedbackButton>
