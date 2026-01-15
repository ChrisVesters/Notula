<script lang="ts" module>
	export const Visibility = {
		PUBLIC: "PUBLIC",
		UNSCOPED: "UNSCOPED",
		SCOPED: "SCOPED"
	} as const;

	export type Visibility = (typeof Visibility)[keyof typeof Visibility];
</script>

<script lang="ts">
	import type { Snippet } from "svelte";
	import { onMount } from "svelte";

	import { goto } from "$app/navigation";

	import Auth from "./Auth";

	interface ProtectedRouteProps {
		children: Snippet;
		visiblity: Visibility;
	}

	let { children, visiblity }: ProtectedRouteProps = $props();
	let allowed: boolean = $state(false);
	let principal = Auth.getPrincipal();

	onMount(() => {
		const isLoggedIn: boolean = $principal?.isValid() ?? false;
		const isScoped = $principal?.isScoped() ?? false;

		console.log($principal, isLoggedIn, isScoped);

		if (isScoped) {
			if (visiblity !== Visibility.PUBLIC) {
				allowed = true;
			} else {
				goto("/dashboard");
			}
		} else if (isLoggedIn) {
			if (visiblity === Visibility.UNSCOPED) {
				allowed = true;
			} else {
				goto("/select-organisation");
			}
		} else {
			if (visiblity === Visibility.PUBLIC) {
				allowed = true;
			} else {
				goto("/");
			}
		}
	});
</script>

{#if allowed}
	{@render children?.()}
{/if}
