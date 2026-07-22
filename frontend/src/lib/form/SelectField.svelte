<script lang="ts">
	import type { FullAutoFill, HTMLInputTypeAttribute } from "svelte/elements";

	import "./form.css";

	export type SelectOptionProps<T> = {
		label: string;
		value: T;
	};

	export type SelectFieldProps<T> = {
		value: string;
		options: Array<SelectOptionProps<T>>;
		label: string;
		id: string;
		required?: boolean;
		error?: string;
		autocomplete: FullAutoFill;
	};

	let {
		value = $bindable(),
		options,
		label,
		id,
		required = false,
		error,
		autocomplete
	}: SelectFieldProps<unknown> = $props();
</script>

<div class="form-group">
	<label for={id}>
		{label}
		{#if required}
			<span class="sup">*</span>
		{/if}
	</label>

	<div class="form-field" class:error>
		<select
			class="field"
			bind:value
			{id}
			name={id}
			{required}
			{autocomplete}
		>
			{#each options as option}
				<option value={option.value}>{option.label}</option>
			{/each}
		</select>
	</div>

	{#if error}
		<div class="error-message">{error}</div>
	{/if}
</div>

<style>
	.field {
		width: 100%;
		background-color: var(--color--background);
		border: none;
		font-size: 1rem;
	}
</style>
