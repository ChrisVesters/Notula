package com.cvesters.notula.credential.dao;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import org.apache.commons.lang3.Validate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.cvesters.notula.credential.bdo.CredentialInfo;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Entity(name = "credentials")
public class CredentialDao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false, updatable = false)
	private long userId;

	@Column(nullable = false)
	private String password;

	public CredentialDao(final long userId, final String password) {
		Objects.requireNonNull(password);

		this.userId = userId;
		this.password = password;
	}

	public CredentialInfo toBdo() {
		Validate.validState(id != null);

		return new CredentialInfo(id, userId);
	}
}