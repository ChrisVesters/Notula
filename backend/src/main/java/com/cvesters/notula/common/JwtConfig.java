package com.cvesters.notula.common;

import java.nio.charset.StandardCharsets;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
public class JwtConfig {

	private final SecretKeySpec keySpec;
	public static final MacAlgorithm MAC_ALGORITHM = MacAlgorithm.HS512;

	public JwtConfig(final @Value("${jwt.secret.key}") String secretKey) {
		this.keySpec = new SecretKeySpec(
				secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
	}

	@Bean
	JwtEncoder jwtEncoder() {
		return new NimbusJwtEncoder(new ImmutableSecret<>(keySpec));
	}

	@Bean
	JwtDecoder jwtDecoder() {
		return NimbusJwtDecoder.withSecretKey(keySpec)
				.macAlgorithm(MacAlgorithm.HS512)
				.build();
	}
}
