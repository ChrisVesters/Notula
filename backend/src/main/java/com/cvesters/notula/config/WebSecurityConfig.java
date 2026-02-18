package com.cvesters.notula.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

	private final JwtAuthConverter authenticationConverter;

	public WebSecurityConfig(final JwtAuthConverter authenticationConverter) {
		this.authenticationConverter = authenticationConverter;
	}

	@Bean
	SecurityFilterChain securityFilterChain(final HttpSecurity http) {
		http.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.csrf(csrf -> csrf.disable());
		http.authorizeHttpRequests(auth -> {
			// WebSocket
			auth.requestMatchers("/ws/**").permitAll();
			// Public
			auth.requestMatchers(HttpMethod.POST, "/api/users").permitAll();
			auth.requestMatchers(HttpMethod.POST, "/api/sessions").permitAll();
			auth.requestMatchers(HttpMethod.POST, "/api/sessions/*/refresh")
					.permitAll();
			// Unscoped
			auth.requestMatchers(HttpMethod.PUT, "/api/sessions/*")
					.authenticated();
			auth.requestMatchers(HttpMethod.GET, "/api/organisations")
					.authenticated();
			auth.requestMatchers(HttpMethod.POST, "/api/organisations")
					.authenticated();
			// Scopes
			auth.anyRequest().hasAuthority(JwtAuthConverter.ORGANISATION_CLAIM);
		});
		http.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt
				.jwtAuthenticationConverter(authenticationConverter)));

		return http.build();
	}

	@Bean
	UrlBasedCorsConfigurationSource corsConfigurationSource() {
		final var configuration = new CorsConfiguration();

		// TODO: move to configuration!
		configuration.setAllowedOrigins(List.of("https://localhost:4443"));
		configuration.setAllowedMethods(List.of("*"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);

		final var source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}