<script lang="ts">
	import type { Snippet } from "svelte";

	export type IconProps = {
		size?: string;
	};

	export type IconRenderProps = IconProps & {
		children: Snippet;
	};

	let { size = "1rem", children }: IconRenderProps = $props();
</script>

<svg
	xmlns="http://www.w3.org/2000/svg"
	width={size}
	height={size}
	fill="currentColor"
	viewBox="0 0 16 16"
>
	{@render children?.()}
</svg>
