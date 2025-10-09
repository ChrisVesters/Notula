import { describe, it, expect } from 'vitest';

import { render, fireEvent } from '@testing-library/svelte';

import PasswordField from '../../../src/lib/form/PasswordField.svelte';

// TODO: proper testing
// Helper to get input and toggle button
function getElements(container) {
	const input = container.querySelector('input');
	const button = container.querySelector('button.toggle-password');
	return { input, button };
}

/**
 * 
 * Render without error
 * Render with error
 * 
 * Typing in input updates value
 * 
 * Toggle visibility
 * Toggle invisibility
 * 
 */

describe('PasswordField', () => {
	it('renders with label and error', () => {
		const { getByLabelText, getByText } = render(PasswordField, {
			props: { value: '', label: 'Secret', error: 'Error!' }
		});
		expect(getByLabelText('Secret')).toBeTruthy();
		expect(getByText('Error!')).toBeTruthy();
	});

	it('binds value and updates on input', async () => {
		let value = '';
		const { container, component } = render(PasswordField, {
			props: { value }
		});
		const { input } = getElements(container);
		await fireEvent.input(input, { target: { value: 'abc12345' } });
		// Svelte 5 runes: value is updated via binding
		expect(input.value).toBe('abc12345');
	});

	it('toggles password visibility', async () => {
		const { container } = render(PasswordField, {
			props: { value: 'mypassword' }
		});
		const { input, button } = getElements(container);
		expect(input.type).toBe('password');
		await fireEvent.click(button);
		expect(input.type).toBe('text');
		await fireEvent.click(button);
		expect(input.type).toBe('password');
	});

	it('shows Eye and EyeSlashed icons correctly', async () => {
		const { container } = render(PasswordField, {
			props: { value: 'mypassword' }
		});
		const { button } = getElements(container);
		// Should show Eye by default
		expect(button.innerHTML).toMatch(/Eye/);
		await fireEvent.click(button);
		// Should show EyeSlashed after toggle
		expect(button.innerHTML).toMatch(/EyeSlashed/);
	});
});
