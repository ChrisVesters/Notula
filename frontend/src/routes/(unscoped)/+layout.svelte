<script lang="ts">
	import ProtectedRoute, {
		Visibility
	} from "$lib/auth/ProtectedRoute.svelte";

	const { children } = $props();
</script>

<ProtectedRoute visiblity={Visibility.UNSCOPED}>
	<main class="container">
		{@render children?.()}
	</main>
</ProtectedRoute>
