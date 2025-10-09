<script lang="ts">
	import type { Component } from "svelte";
	import type { FullAutoFill, HTMLInputTypeAttribute } from "svelte/elements";

	import "./form.css";

	export type AdornmentProps = {
		icon: Component;
		onClick: () => void;
	};

	export type TextFieldProps = {
		value: string;
		label: string;
		id: string;
		type: HTMLInputTypeAttribute;
		required?: boolean;
		error?: string;
		autocomplete: FullAutoFill;
		leftAdornment?: AdornmentProps;
		rightAdornment?: AdornmentProps;
	};

	let {
		value = $bindable(),
		label,
		id,
		type,
		required = false,
		error,
		autocomplete,
		leftAdornment,
		rightAdornment
	}: TextFieldProps = $props();
</script>

<div class="form-group">
	<label for={id}>
		{label}
		{#if required}
			<span class="sup">*</span>
		{/if}
	</label>

	<div class="form-field" class:error>
		{#if leftAdornment}
			<button
				class="adornment"
				type="button"
				onclick={leftAdornment.onClick}
				tabindex="-1"
			>
				<leftAdornment.icon />
			</button>
		{/if}

		<input bind:value {id} name={id} {type} {required} {autocomplete} />

		{#if rightAdornment}
			<button
				class="adornment"
				type="button"
				onclick={rightAdornment.onClick}
				tabindex="-1"
			>
				<rightAdornment.icon />
			</button>
		{/if}
	</div>

	{#if error}
		<div class="error-message">{error}</div>
	{/if}
</div>
