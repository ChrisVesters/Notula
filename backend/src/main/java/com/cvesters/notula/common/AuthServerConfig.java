package com.cvesters.notula.common;

import java.time.Duration;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import com.cvesters.notula.user.UserRepository;

@Configuration
public class AuthServerConfig {

	// @Bean
	// public SecurityFilterChain authServerSecurity(HttpSecurity http)
	// 		throws Exception {
	// 	OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
	// 	return http.build();
	// }

	// What is the redirect URI for the frontend app?
	// What is this even!?
	@Bean
	public RegisteredClientRepository registeredClientRepository() {
		RegisteredClient spaClient = RegisteredClient
				.withId(UUID.randomUUID().toString())
				.clientId("frontend-client")
				.authorizationGrantType(
						AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.redirectUri("http://localhost:5173/callback") // your frontend
																// redirect URI
				.clientAuthenticationMethod(ClientAuthenticationMethod.NONE) // public
																				// SPA
				.tokenSettings(TokenSettings.builder()
						.accessTokenTimeToLive(Duration.ofMinutes(15))
						.refreshTokenTimeToLive(Duration.ofDays(30))
						.reuseRefreshTokens(false)
						.build())
				.build();

		// TODO: should this not be persisted?
		return new InMemoryRegisteredClientRepository(spaClient);
	}

	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer(
			UserRepository users) {
		return context -> {
			if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
				String email = context.getPrincipal().getName();
				var user = users.findByEmail(email).orElseThrow();
				context.getClaims().claim("email", user.getEmail());
				context.getClaims().claim("roles", user.getRoles());
				context.getClaims().claim("tenant", user.getTenantId());
			}
		};
	}

}


/**
 * FLOW:
 * 1. User filles in credentials on the login screen.
 * 2. A call is made to POST /sessions/
 * 3. If successful, the front-end generates a PKCE pair.
 * 4. Redirect the browser to the backend /oauth2/authorize endpoint.
 * 		https://api.myapp.com/oauth2/authorize
  			?client_id=frontend-client
  			&response_type=code
  			&redirect_uri=https://myapp.com/callback
  			&code_challenge=<SHA256 of verifier>
  			&code_challenge_method=S256
 * 5. Spring knows the user is already authenticated (from step 2) and verifies the challenge
 * 6. Spring redirects the browser to the pre-defined callback with a code.
 * 7. The front-end reads the code and calls the /oauth2/token endpoint with the code to get the jwt
 * 8. Spring reads and verifies the request and returns the jwt.
 * 9. The front-end stores the jwt and uses it for subsequent requests.
		Access token → stored in memory (not localStorage!)
		Refresh token → usually HTTP-only cookie or memory
 **/