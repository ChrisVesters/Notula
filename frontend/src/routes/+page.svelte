<script lang="ts">
	import { goto } from "$app/navigation";
	
	import { t } from "$lib/assets/translations/index";

	import PasswordField from "$lib/form/PasswordField.svelte";
	import TextField from "$lib/form/TextField.svelte";

	import SessionClient from "$lib/session/SessionClient";

	let email = $state("");
	let emailError = $state("");
	let password = $state("");
	let passwordError = $state("");

	function login(event: SubmitEvent) {
		event.preventDefault();

		emailError = "";
		passwordError = "";

		if (!email.match(".+@.+")) {
			emailError = $t("common.invalidEmailErrorMessage");
			return;
		}

		if (password.length == 0) {
			passwordError = $t("common.requiredFieldErrorMessage");
			return;
		}

		SessionClient.create({ email, password })
			.then(() => {
				// TODO: redirect?
				goto("/dashboard");
			})
			.catch(error => {
				// TODO: better error handling
				alert("Login failed. Please try again.");
			});
	}
</script>

<main class="landing-flex">
	<section class="landing-left">
		<h1>Welcome to Notula</h1>
		<p class="subtitle">Your notes, organized and accessible anywhere.</p>
	</section>
	<section class="landing-right">
		<form class="login-form" novalidate onsubmit={login}>
			<h2>{$t("common.login")}</h2>

			<TextField
				bind:value={email}
				label={$t("common.email")}
				id="email"
				type="email"
				required={true}
				autocomplete="username"
				error={emailError}
			/>
			<PasswordField
				bind:value={password}
				label={$t("common.password")}
				id="password"
				autocomplete="current-password"
				error={passwordError}
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
