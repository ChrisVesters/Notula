<script lang="ts">
	import { t } from "$lib/assets/translations";

	import IconEdit from "$lib/assets/icons/IconEdit.svelte";

	import IconButton from "$lib/form/IconButton.svelte";

	import type { OrganisationInfo } from "./OrganisationTypes";

	export type OrganisationInfoViewProps = {
		organisation: OrganisationInfo;

		onEdit: () => void;
	};

	const { organisation, onEdit }: OrganisationInfoViewProps = $props();
</script>

<IconButton icon={IconEdit} onClick={onEdit} />
<span>{$t("common.name")}: {organisation.name}</span>
