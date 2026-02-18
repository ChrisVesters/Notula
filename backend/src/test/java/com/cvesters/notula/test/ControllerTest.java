package com.cvesters.notula.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.cvesters.notula.config.JwtConfig;
import com.cvesters.notula.config.WebSecurityConfig;

@Import({ JwtConfig.class, WebSecurityConfig.class })
public abstract class ControllerTest {

	private static final String SERVER = "http://localhost";

	@Autowired
	protected MockMvc mockMvc;

	public String getUrl(final String endpoint) {
		return SERVER + endpoint;
	}
}
