<script lang="ts">
	import ProtectedRoute, {
		Visibility
	} from "$lib/auth/ProtectedRoute.svelte";

	const { children } = $props();
</script>

<ProtectedRoute visiblity={Visibility.PUBLIC}>
	<main class="landing-flex">
		{@render children?.()}
	</main>
</ProtectedRoute>
