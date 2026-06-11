<script lang="ts">
	import type { IconProps } from "$lib/assets/icons/Icon.svelte";
	import type { Component } from "svelte";

	export type IconButtonProps = {
		icon: Component<IconProps>;
		iconProps?: IconProps;

		onClick: () => void;
	};

	let { icon, iconProps, onClick }: IconButtonProps = $props();

	const Icon = $derived(icon);
</script>

<button class="icon" onclick={onClick}>
	<Icon {...iconProps} />
</button>

<style>
	.icon {
		background: none;
		border: none;
		cursor: pointer;
	}

	.icon:hover {
		color: var(--color-primary-500);
	}
</style>
