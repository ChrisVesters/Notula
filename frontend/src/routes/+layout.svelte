<script lang="ts">
	import { onMount } from "svelte";

	import favicon from "$lib/assets/favicon.svg";
	import Auth from "$lib/auth/Auth";

	let { children } = $props();

	let loaded: boolean = $state(false);

	onMount(() => {
		Auth.loadPrincipal();

		loaded = true;
	});
</script>

<svelte:head>
	<link rel="icon" href={favicon} />
</svelte:head>

{#if loaded}
	{@render children?.()}
{/if}
