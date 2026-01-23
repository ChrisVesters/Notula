package com.cvesters.notula.common;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

	private static final String ORGANISATION_CLAIM = "CLAIM_ORGANISATION";

	@Bean
	SecurityFilterChain securityFilterChain(final HttpSecurity http)
			throws Exception {
		http.csrf(csrf -> csrf.disable());
		http.authorizeHttpRequests(auth -> {
			// Public
			auth.requestMatchers(HttpMethod.POST, "/api/users").permitAll();
			auth.requestMatchers(HttpMethod.POST, "/api/sessions").permitAll();
			// Unscoped
			auth.requestMatchers(HttpMethod.PUT, "/api/sessions/*")
					.authenticated();
			auth.requestMatchers(HttpMethod.GET, "/api/organisations")
					.authenticated();
			auth.requestMatchers(HttpMethod.POST, "/api/organisations")
					.authenticated();
			// Scopes
			auth.anyRequest().hasAuthority(ORGANISATION_CLAIM);
		});
		http.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt
				.jwtAuthenticationConverter(jwtAuthenticationConverter())));

		return http.build();
	}

	@Bean
	UrlBasedCorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		configuration.setAllowedOriginPatterns(List.of("*"));
		configuration.setAllowedMethods(List.of("*"));
		configuration.setAllowedHeaders(List.of("*"));
		// configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		final JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(jwt -> {
			final var authorities = new ArrayList<GrantedAuthority>();

			// TODO: no magic strings!
			if (jwt.hasClaim("organisation_id")) {
				authorities.add(new SimpleGrantedAuthority(ORGANISATION_CLAIM));
			}

			return authorities;
		});

		return converter;
	}
}