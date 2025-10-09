<script lang="ts">
	import { t } from "$lib/assets/translations/index";
	import PasswordField from "$lib/form/PasswordField.svelte";
	import TextField from "$lib/form/TextField.svelte";

	let email = $state("");
	let password = $state("");
</script>

<main class="landing-flex">
	<section class="landing-left">
		<h1>Welcome to Notula</h1>
		<p class="subtitle">Your notes, organized and accessible anywhere.</p>
	</section>
	<section class="landing-right">
		<form class="login-form">
			<h2>{$t("common.login")}</h2>

			<TextField
				bind:value={email}
				label={$t("common.email")}
				id="email"
				type="email"
				required={true}
				autocomplete="username"
			/>
			<PasswordField
				bind:value={password}
				label={$t("common.password")}
				id="password"
				autocomplete="current-password"
			/>

			<button type="submit" class="btn primary" style="width:100%"
				>Login</button
			>
			<div class="register-link">
				<span>Don't have an account?</span>
				<a href="/register">Register</a>
			</div>
		</form>
	</section>
</main>
