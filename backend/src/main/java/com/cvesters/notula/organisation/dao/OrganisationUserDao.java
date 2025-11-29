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
import lombok.Setter;

import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;

@Setter
@Getter
@Entity(name = "organisation_users")
public class OrganisationUserDao {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Setter(AccessLevel.NONE)
	private Long id;

	@Column(name = "organisation_id", nullable = false)
	private Long organisationId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	public OrganisationUserDao(
			final OrganisationUserInfo organisationUserInfo) {
		Objects.requireNonNull(organisationUserInfo);

		this.organisationId = organisationUserInfo.getOrganisationId();
		this.userId = organisationUserInfo.getUserId();
	}

	public OrganisationUserInfo toBdo() {
		Validate.validState(id != null);

		return new OrganisationUserInfo(id, organisationId, userId);
	}
}
