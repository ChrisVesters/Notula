<script lang="ts">
	import { onMount } from "svelte";

	import { goto } from "$app/navigation";
	import { t } from "$lib/assets/translations";

	import Session from "$lib/auth/Session";
	import TextField from "$lib/form/TextField.svelte";
	import OrganisationClient from "$lib/organisation/OrganisationClient";
	import type { OrganisationInfo } from "$lib/organisation/OrganisationTypes";
	import SessionClient from "$lib/session/SessionClient";

	let name = $state("");
	let nameError = $state("");
	let organisations: Array<OrganisationInfo> = $state([]);

	onMount(async () => {
		organisations = await OrganisationClient.getAll();
	});

	const selectOrganisation = async (organisationId: number) => {
		try {
		const session = await SessionClient.update(Session.getId(), { organisationId });
		Session.update(session);
		goto("/meetings");
	} catch (error) {

				// TODO: better error handling
				alert("Switching organisation failed. Please try again.");
			}
	}

	const createOrganisation = async (event: SubmitEvent) => {
		event.preventDefault();

		nameError = "";

		if (name.length === 0) {
			nameError = $t("common.requiredFieldErrorMessage");
			return;
		}

		try {
			const organisation = await OrganisationClient.create({ name });
			selectOrganisation(organisation.id);
		} catch (error) {
			// TODO: better error handling:
			alert("Creating organisation failed. Please try again.");
		}
	};
</script>

<section class="card" style="margin-top: 2rem;">
	<h2 class="card-title">{$t("common.selectOrganisation")}</h2>
	{#each organisations as organisation}
		<button
			class="btn primary"
			onclick={() => selectOrganisation(organisation.id)}
		>
			{organisation.name}
		</button>
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
		<button class="btn primary" type="submit">
			{$t("common.create")}
		</button>
	</form>
</section>
