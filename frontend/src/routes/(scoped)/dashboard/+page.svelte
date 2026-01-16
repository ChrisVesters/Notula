<script lang="ts">
	import { t } from "$lib/assets/translations";

	import IconPlus from "$lib/assets/icons/IconPlus.svelte";
</script>

<h1>Dashboard</h1>

<button style="display: flex; align-items:center;">
	<IconPlus />
	{$t("Add")}
</button>
