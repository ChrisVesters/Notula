package com.cvesters.notula.user;

import org.springframework.stereotype.Service;

import com.cvesters.notula.user.bdo.UserInfo;
import com.cvesters.notula.user.bdo.UserLogin;

@Service
public class UserService {


	public UserInfo createUser(final UserLogin userLogin) {

		// TODO: actually persist.
		return new UserInfo(1L, userLogin.getEmail());
	}

}