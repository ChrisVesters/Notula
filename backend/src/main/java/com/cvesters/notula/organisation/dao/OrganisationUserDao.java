package com.cvesters.notula.organisation.dao;

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

import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;

@Getter
@Entity(name = "organisation_users")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrganisationUserDao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "organisation_id", nullable = false)
	private long organisationId;

	@Column(name = "user_id", nullable = false)
	private long userId;

	public OrganisationUserDao(final OrganisationUserInfo bdo) {
		Objects.requireNonNull(bdo);

		this.organisationId = bdo.getOrganisationId();
		this.userId = bdo.getUserId();
	}

	public OrganisationUserInfo toBdo() {
		Validate.validState(id != null);

		return new OrganisationUserInfo(id, organisationId, userId);
	}
}
