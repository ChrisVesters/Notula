<script lang="ts">
	import { trim } from "$lib/common/NameUtils";
	import type { UpdateAction } from "./ActionTypes";
	import { onMount } from "svelte";
	import { compositionHandler } from "./CompositionHandler";
	import { focusHandler } from "./FocusHandler";

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
	let textarea: HTMLTextAreaElement;

	onMount(autoResize);

	const handleFocus = focusHandler({
		onFocusChange: (value: boolean) => (focused = value)
	});

	const handleComposition = compositionHandler({
		onCommit: (action: UpdateAction) => onAction(action)
	});

	function autoResize() {
		textarea.style.height = "auto";
		textarea.style.height = `${textarea.scrollHeight}px`;
	}

	function handleBeforeInput(e: InputEvent) {
		const el = e.target as HTMLTextAreaElement;

		const start = el.selectionStart ?? 0;
		const end = el.selectionEnd ?? start;
		const length = end - start;

		// TODO: https://w3c.github.io/input-events/#interface-InputEvent-Attributes
		// TODO: Extract common logic, but must be based on element.
		// TODO: insertLineBreak not allowed for HtmlInputElement
		switch (e.inputType) {
			case "insertText":
				onAction({
					position: start,
					length: length,
					value: e.data ?? ""
				});
				break;
			case "deleteContentBackward":
				if (start == 0 && length == 0) {
					break;
				}

				onAction({
					position: length > 0 ? start : start - 1,
					length: length > 0 ? length : 1,
					value: ""
				});
				break;
			case "deleteContentForward":
				if (start == value.length) {
					break;
				}

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
			case "insertLineBreak":
				onAction({
					position: start,
					length: length,
					value: "\n"
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

<textarea
	bind:this={textarea}
	{@attach handleComposition}
	{@attach handleFocus}
	class={`full-width text-block ${className}`}
	bind:value
	placeholder={!focused ? trim(value, placeholder) : ""}
	oninput={autoResize}
	onbeforeinput={handleBeforeInput}
>
</textarea>
