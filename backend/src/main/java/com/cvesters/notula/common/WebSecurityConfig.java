package com.cvesters.notula.common;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(final HttpSecurity http)
			throws Exception {
		http.csrf(csrf -> csrf.disable());
		// TODO: Add filter for oauth2
		http.authorizeHttpRequests(
				requests -> requests.anyRequest().permitAll());
		// .anyRequest()
		// .authenticated()).oauth2ResourceServer(oauth2 -> oauth2.jwt());
		return http.build();

		// http
		// .authorizeHttpRequests(auth -> auth
		// .requestMatchers("/api/public/**").permitAll()
		// .anyRequest().authenticated())
		// .oauth2ResourceServer(oauth2 -> oauth2.jwt());
		// return http.build();
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

	// @Bean
	// public JwtAuthenticationConverter jwtAuthenticationConverter() {
	// JwtGrantedAuthoritiesConverter authoritiesConverter = new
	// JwtGrantedAuthoritiesConverter();
	// authoritiesConverter.setAuthoritiesClaimName("roles");
	// authoritiesConverter.setAuthorityPrefix("ROLE_");

	// JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
	// converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
	// return converter;
	// }

	// TODO: WHat do I need?

	// @Bean
	// public SecurityFilterChain filterChain(HttpSecurity http) throws
	// Exception {
	// return http.csrf(AbstractHttpConfigurer::disable)
	// .cors(cors -> cors.configurationSource(corsConfigurationSource()))
	// .authorizeHttpRequests(auth ->
	// auth.requestMatchers("/actuator/health/**")
	// .permitAll()
	// .requestMatchers("/actuator/info/**")
	// .permitAll()
	// .requestMatchers("/v2/api-docs", "/v3/api-docs", "/configuration/ui",
	// "/swagger-resources", "/configuration/security", "/swagger-ui.html",
	// "/webjars/**", "/swagger-resources/configuration/ui",
	// "/swagger-resources/configuration/security")
	// .permitAll()
	// .anyRequest()
	// .authenticated())
	// .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
	// jwt.jwtAuthenticationConverter(customAuthConverter()))
	// .bearerTokenResolver(cookieBearerTokenResolver())
	// )
	// .build();
	// }

	// @Bean
	// Converter<Jwt, ? extends AbstractAuthenticationToken>
	// customAuthConverter() {
	// var rolesConv = new JwtGrantedAuthoritiesConverter();
	// rolesConv.setAuthoritiesClaimName("roles");
	// rolesConv.setAuthorityPrefix("ROLE_");

	// return jwt -> {
	// Collection<GrantedAuthority> authorities = rolesConv.convert(jwt);
	// Integer practiceId =
	// Integer.parseInt(jwt.getClaimAsString("practiceId"));
	// Principal principal = new FugaPrincipal(jwt.getSubject(), practiceId);
	// var auth = new UsernamePasswordAuthenticationToken(principal, "N/A",
	// authorities);
	// auth.setDetails(jwt);
	// return auth;
	// };
	// }

	// @Bean
	// public BearerTokenResolver cookieBearerTokenResolver() {
	// return new CookieBearerTokenResolver();
	// }
}