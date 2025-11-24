package com.cvesters.notula.user.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import org.apache.commons.lang3.Validate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cvesters.notula.common.domain.Email;
import com.cvesters.notula.user.bdo.UserInfo;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Entity(name = "users")
public class UserDao {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Setter(value = AccessLevel.PRIVATE)
	private Long id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column
	private String password;

	public UserDao(final String email, final String password) {
		this.email = email;
		this.password = password;
	}

	public UserInfo toBdo() {
		Validate.validState(id != null);

		final var validatedEmail = new Email(email);
		return new UserInfo(id, validatedEmail);
	}
}
