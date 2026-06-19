<script lang="ts">
	import Loading from "$lib/common/Loading.svelte";

	import OrganisationClient from "./OrganisationClient";
	import OrganisationInfoForm from "./OrganisationInfoForm.svelte";
	import OrganisationInfoView from "./OrganisationInfoView.svelte";
	import type { OrganisationInfo } from "./OrganisationTypes";

	export type OrganisationInfoEditorProps = {
		id: number;
	};

	let edit = $state(false);
	let organisation: OrganisationInfo | undefined = $state();

	const { id }: OrganisationInfoEditorProps = $props();

	$effect(() => {
		async function load() {
			organisation = await OrganisationClient.get(id);
		}

		load();
	});

	const handleOnEdit = (): void => {
		edit = true;
	};

	const handleOnView = (): void => {
		edit = false;
	};

	const handleOnSave = async (): Promise<void> => {
		if (organisation === undefined) {
			return Promise.reject();
		}

		const request = {
			name: organisation.name
		}
		
		try {
			organisation = await OrganisationClient.update(id, organisation);
			edit = false;
		} catch (error) {
			throw new Error();
		}
	};
</script>

{#if organisation !== undefined}
	{#if edit}
		<OrganisationInfoForm
			{organisation}
			onSave={handleOnSave}
			onCancel={handleOnView}
		/>
	{:else}
		<OrganisationInfoView {organisation} onEdit={handleOnEdit} />
	{/if}
{:else}
	<Loading />
{/if}
