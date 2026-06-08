<script lang="ts">
	import { page } from "$app/state";

	import IconDashboard from "$lib/assets/icons/IconDashboard.svelte";
	import IconNotes from "$lib/assets/icons/IconMeetings.svelte";
	import IconSidebarClose from "$lib/assets/icons/IconSidebarClose.svelte";
	import IconSidebarOpen from "$lib/assets/icons/IconSidebarOpen.svelte";
	import { t } from "$lib/assets/translations";

	let isOpen = $state(true);

	const handleToggleOpen = () => {
		isOpen = !isOpen;
	};

	const isActive = (path: string) => {
		return page.url.pathname === path;
	};
</script>

<aside class={`sidebar ${isOpen ? "open" : "closed"}`}>
	<nav class="sidebar-nav">
		<div class="sidebar-header">
			<button
				class="toggle-btn"
				onclick={handleToggleOpen}
				aria-label="Toggle sidebar"
			>
				{#if isOpen}
					<IconSidebarClose />
				{:else}
					<IconSidebarOpen />
				{/if}
			</button>
			<h1>Notula</h1>
		</div>

		<div class="nav-menu">
			<a
				href="/meetings"
				class={`nav-link ${isActive("/meetings") ? "active" : ""}`}
				title="Meetings"
			>
				<IconNotes />
				<span>{$t("common.meetings")}</span>
			</a>
		</div>
	</nav>
</aside>
