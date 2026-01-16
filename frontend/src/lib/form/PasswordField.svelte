<script lang="ts">
	import type { FullAutoFill } from "svelte/elements";

	import IconEye from "$lib/assets/icons/IconEye.svelte";
	import IconEyeSlashed from "$lib/assets/icons/IconEyeSlashed.svelte";

	import "./form.css";
	import TextField from "./TextField.svelte";

	export type PasswordFieldProps = {
		value: string;
		label: string;
		id: string;
		error?: string;
		autocomplete: FullAutoFill;
	};

	let {
		value = $bindable(),
		label,
		id,
		error,
		autocomplete
	}: PasswordFieldProps = $props();

	let show = $state(false);

	function toggleShow() {
		show = !show;
	}
</script>

<TextField
	bind:value
	{label}
	{id}
	type={show ? "text" : "password"}
	required
	{error}
	{autocomplete}
	rightAdornment={{
		icon: show ? IconEyeSlashed : IconEye,
		onClick: toggleShow
	}}
/>
