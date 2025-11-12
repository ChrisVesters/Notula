package com.cvesters.notula.common;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import io.jsonwebtoken.security.Keys;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

	private final String secretKey;

	public WebSecurityConfig(final @Value("${jwt.secret.key}") String secretKey) {
		this.secretKey = secretKey;
	}

	@Bean
	SecurityFilterChain securityFilterChain(final HttpSecurity http)
			throws Exception {
		http.csrf(csrf -> csrf.disable());
		http.authorizeHttpRequests(
				requests -> requests.anyRequest().permitAll());
		// http.oauth2ResourceServer(oauth -> oauth.jwt());

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
	SecretKey jwtSecretKey() {
		return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
	}

	// @Bean
	// JwtDecoder jwtDecoder() {
	// 	return NimbusJwtDecoder.withSecretKey(jwtSecretKey()).build();
	// }
}