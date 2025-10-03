package com.cvesters.notula.organisation.dao;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import com.cvesters.notula.organisation.bdo.Organisation;

@Setter
@Getter
@Entity(name = "organisation")
public class OrganisationDao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Setter(AccessLevel.NONE)
	private Long id;

	@Column
	private String name;

	public static OrganisationDao fromBdo(final Organisation bdo) {
		Objects.requireNonNull(bdo);

		final var dao = new OrganisationDao();
		dao.name = bdo.getName();

		return dao;
	}

	public Organisation toBdo() {
		// TODO: id!!!
		return new Organisation(name);
	}
}
