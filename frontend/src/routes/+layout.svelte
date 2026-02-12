<script lang="ts">
	import { onMount } from "svelte";

	import favicon from "$lib/assets/favicon.svg";
	import Session from "$lib/auth/Session";

	let { children } = $props();

	let loaded: boolean = $state(false);

	onMount(() => {
		Session.load();

		loaded = true;
	});
</script>

<svelte:head>
	<link rel="icon" href={favicon} />
</svelte:head>

{#if loaded}
	{@render children?.()}
{/if}
