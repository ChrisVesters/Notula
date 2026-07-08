<script lang="ts">
	import { t } from "$lib/assets/translations";

	import SideMenu from "$lib/menu/SideMenu.svelte";
	import IconGear from "$lib/assets/icons/IconGear.svelte";
	import IconUsers from "$lib/assets/icons/IconUsers.svelte";

	let { children } = $props();
</script>

<div class="layout">
	<SideMenu
		items={[
			{
				path: "/general",
				label: $t("common.general"),
				icon: IconGear
			},
			{
				path: "/users",
				label: $t("common.users"),
				icon: IconUsers
			}
		]}
	/>

	<main class="content">
		{@render children()}
	</main>
</div>

<style>
	.layout {
		display: flex;
		height: 100%;
	}

	.content {
		flex: 1;
		padding: 1rem;
	}
</style>
