<script lang="ts">
	import Eye from "$lib/assets/icons/Eye.svelte";
	import EyeSlashed from "$lib/assets/icons/EyeSlashed.svelte";

	import UserClient from "$lib/user/UserClient";

	import "./register.css";

	let email = $state("");
	let password = $state("");
	let passwordError = $state("");
	let repeatPassword = $state("");
	let repeatPasswordError = $state("");

	let showPassword = $state(false);
	let showRepeatPassword = $state(false);

	function register(event: SubmitEvent) {
		event.preventDefault();

		passwordError = "";
		repeatPasswordError = "";
		if (password.length < 8) {
			passwordError = "Password must be at least 8 characters.";
			return;
		}

		if (password !== repeatPassword) {
			repeatPasswordError = "Passwords do not match.";
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
		<h1>Create your Notula account</h1>
		<p class="subtitle">Start organizing your notes today.</p>
	</section>
	<section class="landing-right">
		<form class="register-form" onsubmit={register}>
			<h2>Register</h2>
			<div class="form-group">
				<label for="email">Email</label>
				<input
					bind:value={email}
					id="email"
					name="email"
					type="email"
					required
					autocomplete="username"
				/>
			</div>
			<div class="form-group password-group">
				<label for="password">Password</label>
				<div class="password-wrapper">
					<input
						bind:value={password}
						id="password"
						name="password"
						type={showPassword ? "text" : "password"}
						required
						autocomplete="new-password"
					/>
					<button
						type="button"
						class="toggle-password"
						onclick={() => (showPassword = !showPassword)}
						aria-label={showPassword
							? "Hide password"
							: "Show password"}
						tabindex="-1"
					>
						{#if showPassword}
							<EyeSlashed />
						{:else}
							<Eye />
						{/if}
					</button>
				</div>
				{#if passwordError}
					<div class="error-message">{passwordError}</div>
				{/if}
			</div>
			<div class="form-group password-group">
				<label for="repeat-password">Repeat Password</label>
				<div class="password-wrapper">
					<input
						bind:value={repeatPassword}
						id="repeat-password"
						name="repeat-password"
						type={showRepeatPassword ? "text" : "password"}
						required
						autocomplete="new-password"
					/>
					<button
						type="button"
						class="toggle-password"
						onclick={() =>
							(showRepeatPassword = !showRepeatPassword)}
						aria-label={showRepeatPassword
							? "Hide password"
							: "Show password"}
						tabindex="-1"
					>
						{#if showRepeatPassword}
							<EyeSlashed />
						{:else}
							<Eye />
						{/if}
					</button>
				</div>
				{#if repeatPasswordError}
					<div class="error-message">{repeatPasswordError}</div>
				{/if}
			</div>
			<button type="submit" class="btn primary" style="width:100%">
				Register
			</button>
			<div class="login-link">
				<span>Already have an account?</span>
				<a href="/">Login</a>
			</div>
		</form>
	</section>
</main>