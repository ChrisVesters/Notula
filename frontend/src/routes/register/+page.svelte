<script lang="ts">
	import { t } from "$lib/assets/translations/index";

	import PasswordField from "$lib/form/PasswordField.svelte";
	import TextField from "$lib/form/TextField.svelte";

	import UserClient from "$lib/user/UserClient";

	let email = $state("");
	let emailError = $state("");
	let password = $state("");
	let passwordError = $state("");
	let repeatPassword = $state("");
	let repeatPasswordError = $state("");

	function register(event: SubmitEvent) {
		event.preventDefault();

		passwordError = "";
		repeatPasswordError = "";
		emailError = "";

		if (!email.match(".+@.+")) {
			emailError = $t("common.invalidEmailErrorMessage");
			return;
		}

		if (password.length < 8) {
			passwordError = $t("common.passwordLengthErrorMessage");
			return;
		}

		if (password !== repeatPassword) {
			repeatPasswordError = $t("common.passwordsDoNotMatchErrorMessage");
			return;
		}

		UserClient.create({ email, password })
			.then(() => {
				// TODO: redirect?
				window.location.href = "/";
			})
			.catch(error => {
				// TODO: better error handling
				alert("Registration failed. Please try again.");
			});
	}
</script>

<main class="landing-flex">
	<section class="landing-left">
		<h1>{$t("common.createAccountMessage")}</h1>
		<p class="subtitle">{$t("common.startTodayMessage")}</p>
	</section>
	<section class="landing-right">
		<form class="login-form" novalidate onsubmit={register}>
			<h2>{$t("common.register")}</h2>
			<TextField
				bind:value={email}
				label={$t("common.email")}
				id="email"
				type="email"
				required
				autocomplete="username"
				error={emailError}
			/>
			<PasswordField
				bind:value={password}
				label={$t("common.password")}
				id="password"
				error={passwordError}
				autocomplete="new-password"
			/>
			<PasswordField
				bind:value={repeatPassword}
				label="Repeat Password"
				id="repeat-password"
				error={repeatPasswordError}
				autocomplete="new-password"
			/>
			<button type="submit" class="primary full-width">
				{$t("common.register")}
			</button>

			<div class="register-link">
				<span>{$t("common.alreadyAccountMessage")}</span>
				<a href="/">{$t("common.login")}</a>
			</div>
		</form>
	</section>
</main>
