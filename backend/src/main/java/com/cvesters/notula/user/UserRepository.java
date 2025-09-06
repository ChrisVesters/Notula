package com.cvesters.notula.user;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import com.cvesters.notula.user.dao.UserDao;


// TODO: split between UserLogin and UserInfo
public interface UserRepository extends Repository<UserDao, Long> {
	
	Optional<UserDao> findByEmail(String email);
}
