package com.cvesters.notula.user;

import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

	private static final String ENDPOINT = "/api/users";

	private static final TestUser USER = TestUser.EDUARDO_CHRISTIANSEN;

	@Autowired
	protected MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Nested
	class Create {

		// @Test
		// void success() {
		// 	when(userService.createUser(...)).thenReturn(USER.info());

		// 	mockMvc.
		// 	.andExpect(jsonPath($.id), ...);
		// }
	}
}
