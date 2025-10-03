<script lang="ts">
	import { t } from "$lib/assets/translations/index";

</script>

<main class="landing-flex">
	<section class="landing-left">
		<h1>Welcome to Notula</h1>
		<p class="subtitle">Your notes, organized and accessible anywhere.</p>
	</section>
	<section class="landing-right">
		<form class="login-form">
			<h2>{$t("common.login")}</h2>
			<div class="form-group">
				<label for="email">{$t("common.email")}</label>
				<input
					id="email"
					name="email"
					type="email"
					required
					autocomplete="username"
				/>
			</div>
			<div class="form-group">
				<label for="password">Password</label>
				<input
					id="password"
					name="password"
					type="password"
					required
					autocomplete="current-password"
				/>
			</div>
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
