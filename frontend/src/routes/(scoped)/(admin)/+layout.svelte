<script lang="ts">
	import { onMount } from "svelte";

	import { goto } from "$app/navigation";

	import Auth from "$lib/auth/Auth";

	import { OrganisationUserRole } from "$lib/organisation/OrganisationUserTypes";

	const { children } = $props();

	let allowed: boolean = $state(false);
	let principal = Auth.getPrincipal();

	onMount(async () => {
		allowed = $principal?.role === OrganisationUserRole.ADMIN;

		if (!allowed) {
			goto("/");
		}
	});
</script>

{#if allowed}
	{@render children?.()}
{/if}
