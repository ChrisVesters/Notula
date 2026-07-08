<script lang="ts">
	import type { Component, Snippet } from "svelte";
	import { page } from "$app/state";

	import type { IconProps } from "$lib/assets/icons/Icon.svelte";

	export type MenuItem = {
		path: string;
		icon: Component<IconProps>;
		label: string;
	};

	export interface SideMenuProps {
		items: MenuItem[];
	}

	let { items }: SideMenuProps = $props();

	const base = $derived(page.url.pathname.split("/").slice(0, -1).join("/"));
</script>

<nav class="sidemenu">
	{#each items as item}
		<a
			href={`${base}${item.path}`}
			class:active={page.url.pathname.endsWith(item.path)}
		>
			<item.icon />
			<span>{item.label}</span>
		</a>
	{/each}
</nav>

<style>
	.sidemenu {
		width: 220px;
		padding-top: 2.5rem;
		display: flex;
		flex-direction: column;
		border-right: 1px solid #ddd;
	}

	.sidemenu a {
		display: flex;
		align-items: center;
		gap: 0.75rem;
		padding: 0.75rem 1rem;
		text-decoration: none;
		color: inherit;
	}

	.sidemenu a.active {
		font-weight: 600;
		background: #eee;
	}
</style>
