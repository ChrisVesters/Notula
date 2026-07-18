<script lang="ts">
	import { goto } from "$app/navigation";
	import { page } from "$app/state";

	import { t } from "$lib/assets/translations";

	import IconLogout from "$lib/assets/icons/IconLogOut.svelte";
	import IconNotes from "$lib/assets/icons/IconMeetings.svelte";
	import IconOrganisationSettings from "$lib/assets/icons/IconOrganisationSettings.svelte";
	import IconSidebarClose from "$lib/assets/icons/IconSidebarClose.svelte";
	import IconSidebarOpen from "$lib/assets/icons/IconSidebarOpen.svelte";
	import IconSwitchOrganisation from "$lib/assets/icons/IconSwitchOrganisation.svelte";

	import Auth from "$lib/auth/Auth";
	import Session from "$lib/auth/Session";
	import SessionClient from "$lib/session/SessionClient";

	let isOpen = $state(true);
	let principal = Auth.getPrincipal();

	const handleToggleOpen = () => {
		isOpen = !isOpen;
	};

	const isActive = (path: string) => {
		return (
			page.url.pathname === path ||
			page.url.pathname.startsWith(path + "/")
		);
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

		<div class="sidebar-menu">
			{#if $principal?.isScoped()}
				<a
					href="/meetings"
					class={`sidebar-link ${isActive("/meetings") ? "active" : ""}`}
					title="Meetings"
				>
					<IconNotes />
					<span>{$t("common.meetings")}</span>
				</a>
			{/if}
		</div>

		<div class="sidebar-footer">
			{#if $principal?.isScoped()}
				<a
					href="/organisation"
					class={`sidebar-link ${isActive("/organisation") ? "active" : ""}`}
					aria-label="Organisation"
				>
					<IconOrganisationSettings />
					<span>{$t("common.organisation")}</span>
				</a>

				<a
					href="/select-organisation"
					class={`sidebar-link ${isActive("/select-organisation") ? "active" : ""}`}
					aria-label="Switch organisation"
				>
					<IconSwitchOrganisation />
					<span>{$t("common.switchOrganisation")}</span>
				</a>
			{/if}

			<button
				class="toggle-btn sidebar-link"
				onclick={handleLogOut}
				aria-label="Log out"
			>
				<IconLogout />
				<span>{$t("common.logout")}</span>
			</button>
		</div>
	</nav>
</aside>

<style>
	.sidebar {
		background-color: var(--color-primary-100);
		color: var(--color-primary-600);
		transition: width 0.3s ease;
		flex-shrink: 0;
	}

	.sidebar.open {
		width: 12rem;
	}

	.sidebar.closed {
		width: 3rem;
	}

	.sidebar-nav {
		display: flex;
		flex-direction: column;
		height: 100%;
	}

	.sidebar-header {
		display: flex;
		align-items: center;
		padding: 0.5rem 1rem;
		border-bottom: 1px solid var(--color-primary-600);
	}

	.sidebar-header h1 {
		margin: 0;
		font-size: 1.5rem;
		font-weight: 700;
		white-space: nowrap;
		opacity: 1;
		transition: opacity 0.3s ease;
		overflow: hidden;
	}

	.sidebar.closed .sidebar-header h1 {
		opacity: 0;
		width: 0;
		flex: 0;
	}

	.sidebar-menu {
		margin: 0;
		padding: 0.5rem 0;
		flex: 1;
		display: flex;
		flex-direction: column;
	}

	.sidebar-link {
		display: flex;
		align-items: center;
		padding: 0.5rem 1rem;
		color: var(--color-primary-600);
		text-decoration: none;
		transition:
			background-color 0.3s ease,
			padding-left 0.3s ease;
	}

	.sidebar-link:hover {
		background-color: var(--color-primary-200);
	}

	.sidebar-link.active {
		background-color: var(--color-primary-300);
		color: var(--color-primary-700);
		font-weight: 600;
	}

	.sidebar-footer {
		display: flex;
		flex-direction: column;
		align-items: stretch;
		margin: 0;
		padding: 0.5rem 0;
		border-top: 1px solid var(--color-primary-600);
	}

	.sidebar-footer button {
		display: flex;
		justify-content: flex-start;
		padding: 0.5rem 1rem;
		color: var(--color-primary-600);
		text-decoration: none;
		transition:
			background-color 0.3s ease,
			padding-left 0.3s ease;
	}

	.sidebar-link span,
	.sidebar-footer span {
		flex: 1;
		text-align: left;
		white-space: nowrap;
		opacity: 1;
		overflow: hidden;
	}

	.sidebar.open .sidebar-header,
	.sidebar.open .sidebar-footer,
	.sidebar.open .sidebar-link {
		column-gap: 0.75rem;
	}

	.sidebar.closed .sidebar-link span,
	.sidebar.closed .sidebar-footer span {
		opacity: 0;
		width: 0;
		flex: 0;
	}

	.toggle-btn {
		background: none;
		border: none;
		color: currentColor;
		cursor: pointer;
		padding: 0rem;
		display: flex;
		align-items: center;
		justify-content: center;
		transition: color 0.3s ease;
		flex-shrink: 0;
	}

	.toggle-btn span {
		font-family: var(--body-font);
		font-weight: 500;
		font-size: 1rem;
	}
</style>
