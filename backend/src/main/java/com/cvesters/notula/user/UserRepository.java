package com.cvesters.notula.user;

import org.springframework.data.repository.Repository;

import com.cvesters.notula.user.dao.UserDao;

public interface UserRepository extends Repository<UserDao, Long> {

	UserDao save(final UserDao user);
}
