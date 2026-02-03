<script lang="ts">
	import type { Snippet } from "svelte";

	import "./form.css";

	export type FeedbackButtonProps = {
		className?: string;
		onClick: () => Promise<void>;
		children: Snippet;
	};

	let { className, onClick, children }: FeedbackButtonProps = $props();

	let pending = $state(false);

	function handleClick(e: MouseEvent): void {
		if (pending) {
			return;
		}

		pending = true;
		onClick().finally(() => {
			pending = false;
		});
	}
</script>

<button
	class={`btn ${className}`}
	style="display: grid; grid-template-areas: 'content'; place-items: center;"
	type="button"
	aria-busy={pending}
	disabled={pending}
	onclick={handleClick}
>
	<span
		style={`grid-area: content; visibility: ${pending ? "visible" : "hidden"}`}
		class="spinner"
		aria-hidden="true"
	>
	</span>
	<span
		style={`grid-area: content; visibility: ${pending ? "hidden" : "visible"}`}
	>
		{@render children?.()}
	</span>
</button>
