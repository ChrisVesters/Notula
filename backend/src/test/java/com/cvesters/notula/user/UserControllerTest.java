package com.cvesters.notula.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
public class UserControllerTest {

	private static final String ENDPOINT = "/api/users";

	@Autowired
	protected MockMvc mockMvc;

	@MockitoBean
	private UserRepository userRepository;
}
