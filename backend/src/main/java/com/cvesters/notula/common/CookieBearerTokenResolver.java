package com.cvesters.notula.common;

import java.util.Arrays;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.util.StringUtils;

// TODO: investigate if all needed/required.
public class CookieBearerTokenResolver implements BearerTokenResolver {

	@Override
	public String resolve(HttpServletRequest request) {
		return fromCookie(request).or(() -> fromHeader(request)).orElse(null);
	}

	private Optional<String> fromHeader(HttpServletRequest request) {
		String authorizationHeader = request
				.getHeader(HttpHeaders.AUTHORIZATION);
		if (StringUtils.hasText(authorizationHeader)
				&& authorizationHeader.startsWith("Bearer ")) {
			return Optional.of(authorizationHeader.substring(7));
		}
		return Optional.empty();
	}

	private Optional<String> fromCookie(HttpServletRequest request) {
		if (request.getCookies() == null) {
			return Optional.empty();
		}
		return Arrays.stream(request.getCookies())
				.filter(cookie -> "JWT".equals(cookie.getName()))
				.map(Cookie::getValue)
				.filter(StringUtils::hasText)
				.findFirst();
	}
}
