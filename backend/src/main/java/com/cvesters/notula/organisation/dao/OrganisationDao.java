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

import com.cvesters.notula.organisation.bdo.OrganisationInfo;

@Getter
@Entity(name = "organisations")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrganisationDao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	public OrganisationDao(final OrganisationInfo bdo) {
		Objects.requireNonNull(bdo);

		this.name = bdo.getName();
	}

	public OrganisationInfo toBdo() {
		Validate.validState(id != null);

		return new OrganisationInfo(id, name);
	}
}
