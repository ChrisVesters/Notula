<script lang="ts">
	import { goto } from "$app/navigation";

	import { t } from "$lib/assets/translations/index";
	import Auth from "$lib/auth/Auth";
	import DataStorage from "$lib/common/DataStorage";
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

		if (password.length === 0) {
			passwordError = $t("common.requiredFieldErrorMessage");
			return;
		}

		SessionClient.create({ email, password })
			.then(session => {
				DataStorage.setItem("sessionId", session.id.toString());
				Auth.updatePrincipal(session.accessToken);
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
		<h1>{$t("common.welcomeMessage")}</h1>
		<p class="subtitle">{$t("common.effectiveMeetingsMessage")}</p>
	</section>
	<section class="landing-right card">
		<h2 class="card-title">{$t("common.login")}</h2>

		<form novalidate onsubmit={login}>
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
			<button type="submit" class="btn primary" style="width:100%">
				{$t("common.login")}
			</button>
		</form>

		<div class="register-link">
			<span>{$t("common.noAccountMessage")}</span>
			<a href="/register">{$t("common.register")}</a>
		</div>
	</section>
</main>
