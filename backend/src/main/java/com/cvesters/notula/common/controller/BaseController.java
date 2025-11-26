package com.cvesters.notula.common.controller;

import java.net.URI;
import java.util.Objects;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public abstract class BaseController {

	protected URI getLocation(final String path, final Object... uriVariables) {
		Objects.requireNonNull(path);
		Objects.requireNonNull(uriVariables);

		return ServletUriComponentsBuilder.fromCurrentRequest()
				.path(path)
				.buildAndExpand(uriVariables)
				.toUri();
	}
}
