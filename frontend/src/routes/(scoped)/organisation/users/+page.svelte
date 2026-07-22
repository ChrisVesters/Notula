<script lang="ts">
	import { t } from "$lib/assets/translations";

	import Loading from "$lib/common/Loading.svelte";
	import FeedbackButton from "$lib/form/FeedbackButton.svelte";
	import SelectField from "$lib/form/SelectField.svelte";
	import TextField from "$lib/form/TextField.svelte";

	import OrganisationUserClient from "$lib/organisation/OrganisationUserClient";
	import { OrganisationUserRole } from "$lib/organisation/OrganisationUserTypes";
	import type {
		OrganisationUserCreateRequest,
		OrganisationUserView
	} from "$lib/organisation/OrganisationUserTypes";

	let users: Array<OrganisationUserView> | undefined = $state();

	let email = $state("");
	let role = $state(OrganisationUserRole.MEMBER);

	let dialog: HTMLDialogElement;

	$effect(() => {
		async function load() {
			users = await OrganisationUserClient.getAll();
		}

		load();
	});

	const addUser = async (): Promise<void> => {
		// TODO: check email uniqueness
		try {
			const request: OrganisationUserCreateRequest = {
				email,
				role
			};

			const user = await OrganisationUserClient.create(request);
			users?.push({
				...user,
				email
			});
			dialog.close();
		} catch (error) {
			// TODO: better error handling
			alert("Adding user failed. Please try again.");
		}
	};
</script>

<h1>{$t("common.users")}</h1>
<!-- TODO: better UI -->
<button class="btn primary" onclick={() => dialog.showModal()}>
	{$t("common.addObject", { object: $t("common.user") })}
</button>

{#if users !== undefined}
	<table>
		<thead>
			<tr>
				<th>{$t("common.email")}</th>
				<th>{$t("common.role")}</th>
			</tr>
		</thead>

		<tbody>
			{#each users as user}
				<tr>
					<td>{user.email}</td>
					<td>{$t(`roles.${user.role}`)}</td>
				</tr>
			{/each}
		</tbody>
	</table>
{:else}
	<Loading />
{/if}

<dialog bind:this={dialog}>
	<!-- TODO: extract to component -->
	<form>
		<TextField
			bind:value={email}
			label={$t("common.email")}
			id="email"
			type="email"
			required={true}
			autocomplete="email"
		/>

		<SelectField
			bind:value={role}
			options={Object.values(OrganisationUserRole).map(v => ({
				value: v,
				label: $t(`roles.${v}`)
			}))}
			label={$t("common.role")}
			id="role"
			required={true}
			autocomplete="off"
		/>
	</form>
	<button class="btn secondary" onclick={() => dialog.close()}>
		{$t("common.cancel")}
	</button>
	<FeedbackButton className="primary" onClick={addUser}>
		{$t("common.add")}
	</FeedbackButton>
</dialog>

<style>
	table {
		border-collapse: collapse;
		width: 100%;
	}

	th,
	td {
		border: 1px solid #ddd;
		padding: 8px;
		text-align: left;
	}

	th {
		background-color: #f2f2f2;
	}

	tr:nth-child(even) {
		background-color: #f9f9f9;
	}

	tr:hover {
		background-color: #e9e9e9;
	}

	td {
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}
</style>
