<script lang="ts">
	import ProtectedRoute, {
		Visibility
	} from "$lib/auth/ProtectedRoute.svelte";

	const { children } = $props();
</script>

<ProtectedRoute visiblity={Visibility.UNSCOPED}>
	{@render children?.()}
</ProtectedRoute>
