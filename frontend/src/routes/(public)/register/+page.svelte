<script lang="ts">
	import { goto } from "$app/navigation";

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

	function register(event: SubmitEvent): void {
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
				goto("/");
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
	<section class="landing-right card">
		<h2 class="card-title">{$t("common.register")}</h2>

		<form novalidate onsubmit={register}>
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
			<button class="primary full-width" type="submit">
				{$t("common.register")}
			</button>
		</form>

		<div class="register-link">
			<span>{$t("common.alreadyAccountMessage")}</span>
			<a href="/">{$t("common.login")}</a>
		</div>
	</section>
</main>
