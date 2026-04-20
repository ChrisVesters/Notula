<script lang="ts">
	import { trim } from "$lib/common/NameUtils";
	import type { UpdateAction } from "./ActionTypes";

	export type InputProps = {
		className?: string;
		value: string;
		placeholder?: string;
		onAction: (action: UpdateAction) => void;
	};

	let {
		value = $bindable(),
		placeholder,
		className,
		onAction
	}: InputProps = $props();
	let focused: boolean = $state(false);

	function handleBeforeInput(e: InputEvent) {
		const el = e.target as HTMLInputElement;

		const start = el.selectionStart ?? 0;
		const end = el.selectionEnd ?? start;
		const length = end - start;

		// TODO: https://w3c.github.io/input-events/#interface-InputEvent-Attributes

		switch (e.inputType) {
			case "insertText":
				onAction({
					position: start,
					length: length,
					value: e.data ?? ""
				});
				break;
			case "deleteContentBackward":
				onAction({
					position: length > 0 ? start : start - 1,
					length: length > 0 ? length : 1,
					value: ""
				});
				break;
			case "deleteContentForward":
				onAction({
					position: start,
					length: length > 0 ? length : 1,
					value: ""
				});
				break;
			case "insertFromPaste":
				// const pasted = e.data ?? (e as any).dataTransfer?.getData("text") ?? "";
				onAction({
					position: start,
					length: length,
					value: e.data ?? ""
				});
				break;
			case "deleteByCut":
				onAction({
					position: start,
					length: length,
					value: ""
				});
				break;
			default:
				console.log("Unhandled:", e.inputType);
				// TODO: implement undo/redo
				// historyUndo / historyRedo
				e.preventDefault();
				break;
		}
	}
</script>

<input
	class={className}
	bind:value
	placeholder={!focused ? trim(value, placeholder) : ""}
	onfocus={() => (focused = true)}
	onblur={() => (focused = false)}
	onbeforeinput={handleBeforeInput}
/>
