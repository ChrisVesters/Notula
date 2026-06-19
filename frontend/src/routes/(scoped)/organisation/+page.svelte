<script lang="ts">
	import { t } from "$lib/assets/translations";
	import { get } from "svelte/store";

	import Auth, { Principal } from "$lib/auth/Auth";

	import OrganisationInfoEditor from "$lib/organisation/OrganisationInfoEditor.svelte";

	let principal: Principal | null = get(Auth.getPrincipal());
</script>

<h1>{$t("common.organisation")}</h1>

{#if principal === null || principal.organisationId === null}
	<!-- TODO: redirect to login -->
{:else}
	<OrganisationInfoEditor id={principal.organisationId} />
{/if}
