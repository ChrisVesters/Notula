<script lang="ts">
	import { goto } from "$app/navigation";
	import { page } from "$app/state";

	import { t } from "$lib/assets/translations";

	import IconLogout from "$lib/assets/icons/IconLogOut.svelte";
	import IconNotes from "$lib/assets/icons/IconMeetings.svelte";
	import IconSidebarClose from "$lib/assets/icons/IconSidebarClose.svelte";
	import IconSidebarOpen from "$lib/assets/icons/IconSidebarOpen.svelte";
	import IconSwitchOrganisation from "$lib/assets/icons/IconSwitchOrganisation.svelte";
	
	import Session from "$lib/auth/Session";
	import SessionClient from "$lib/session/SessionClient";

	let isOpen = $state(true);

	const handleToggleOpen = () => {
		isOpen = !isOpen;
	};

	const isActive = (path: string) => {
		return page.url.pathname === path;
	};

	const handleLogOut = () => {
		SessionClient.delete(Session.getId()).finally(() => {
			Session.delete();
			goto("/");
		});
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

		<div class="sidebar-footer">
			<a
				href="/select-organisation"
				class="toggle-btn nav-link"
				aria-label="Switch organisation"
			>
				<IconSwitchOrganisation />
				<span>{$t("common.switchOrganisation")}</span>
			</a>

			<button
				class="toggle-btn nav-link"
				onclick={handleLogOut}
				aria-label="Log out"
			>
				<IconLogout />
				<span>{$t("common.logout")}</span>
			</button>
		</div>
	</nav>
</aside>
