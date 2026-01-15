<script lang="ts">
	import { onMount } from "svelte";
	import { goto } from "$app/navigation";
	import { t } from "$lib/assets/translations";

	import TextField from "$lib/form/TextField.svelte";
	import OrganisationClient from "$lib/organisation/OrganisationClient";
	import type { OrganisationInfo } from "$lib/organisation/OrganisationTypes";

	let name = $state("");
	let nameError = $state("");
	let organisations: Array<OrganisationInfo> = $state([]);

	onMount(async () => {
		organisations = await OrganisationClient.getAll();
	});

	function selectOrganisation(organisationId: number) {
		console.log("TODO");
	}

	function createOrganisation(event: SubmitEvent) {
		event.preventDefault();

		nameError = "";

		if (name.length === 0) {
			nameError = $t("common.requiredFieldErrorMessage");
			return;
		}

		OrganisationClient.create({ name })
			.then(organisation => {
				// TODO: refresh token with created organisation.
				// Then redirect to dashboard
				goto("/dashboard");
			})
			.catch(error => {
				// TODO: better error handling:
				alert("Creating organisation failed. Please try again.");
			});
	}
</script>

<main class="container">
	<section class="card">
		<h2 class="card-title">{$t("common.selectOrganisation")}</h2>
		{#each organisations as organisation}
			<button onclick={() => selectOrganisation(organisation.id)}
				>{organisation.name}</button
			>
		{/each}

		<h2 class="card-title">{$t("common.createOrganisation")}</h2>
		<form novalidate onsubmit={createOrganisation}>
			<TextField
				bind:value={name}
				label={$t("common.name")}
				id="name"
				type="text"
				required
				error={nameError}
				autocomplete="organization"
			/>
			<button class="primary full-width" type="submit">
				{$t("common.create")}
			</button>
		</form>
	</section>
</main>
