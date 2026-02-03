import { describe, it, expect } from 'vitest';
import { render, fireEvent, waitFor } from '@testing-library/svelte';
import FeedbackButton from '../../../src/lib/form/FeedbackButton.svelte';

function deferred<T>() {
	let resolve: (value: T | PromiseLike<T>) => void = () => {};
	let reject: (reason?: any) => void = () => {};
	const promise = new Promise<T>((res, rej) => {
		resolve = res;
		reject = rej;
	});
	return { promise, resolve, reject };
}

describe('FeedbackButton', () => {
	it('renders children prop', () => {
		const { getByText } = render(FeedbackButton, { props: { children: () => 'Save' } });
		expect(getByText('Save')).toBeTruthy();
	});

	it('children prop takes precedence over slot', () => {
		const { queryByText } = render(FeedbackButton, {
			props: { children: () => 'Save' },
			slots: { default: 'SlotText' }
		});

		expect(queryByText('Save')).toBeTruthy();
		expect(queryByText('SlotText')).toBeNull();
	});

	it('disables and shows spinner while onClick promise is pending and re-enables after resolve', async () => {
		const d = deferred<void>();
		const onClick = () => d.promise;

		const { container } = render(FeedbackButton, { props: { onClick, children: () => 'Save' } });
		const button = container.querySelector('button') as HTMLButtonElement;

		expect(button.disabled).toBe(false);

		await fireEvent.click(button);

		expect(button.disabled).toBe(true);
		expect(container.querySelector('.spinner')).toBeTruthy();

		d.resolve();

		await waitFor(() => expect(button.disabled).toBe(false));
		await waitFor(() => expect(container.querySelector('.spinner')).toBeNull());
	});

	it('re-dispatches click if no onClick prop provided', async () => {
		const { container, component } = render(FeedbackButton, { props: {}, slots: { default: 'Send' } });
		const button = container.querySelector('button') as HTMLButtonElement;

		let clicked = false;
		component.$on('click', () => { clicked = true; });

		await fireEvent.click(button);
		expect(clicked).toBe(true);
	});
});
