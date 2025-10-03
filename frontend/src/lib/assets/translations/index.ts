import i18n from 'sveltekit-i18n';
import type { Config } from "sveltekit-i18n";

const languages = ["en"];
const namespaces = ["common"];

export const defaultLocale = languages[0];

export type Payload = {
	[key: string]: string;
  };

export const config: Config<Payload> = {
	loaders: languages.flatMap(lng =>
		namespaces.map(namespace => ({
			locale: lng,
			key: namespace,
			loader: async () => (
				await import(`./${lng}/${namespace}.json`)
			).default
		}))
	)
};

export const { t, loading, locales, locale, translations, loadTranslations, addTranslations, setLocale, setRoute } = new i18n(config);
