<script lang="ts">
	import ProtectedRoute, {
		Visibility
	} from "$lib/auth/ProtectedRoute.svelte";
	import Sidebar from "$lib/menu/Sidebar.svelte";

	const { children } = $props();
</script>

<ProtectedRoute visiblity={Visibility.SCOPED}>
	<Sidebar />

	<main class="container">
		<div class="content">
			{@render children?.()}
		</div>
	</main>
</ProtectedRoute>
