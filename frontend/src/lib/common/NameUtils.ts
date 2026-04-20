export function trim(text: string, placeholder?: string): string {
	return text.trim().length > 0 ? text.trim() : (placeholder ?? "");
}
