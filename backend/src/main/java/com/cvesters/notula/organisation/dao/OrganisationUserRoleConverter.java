package com.cvesters.notula.organisation.dao;

import java.util.Objects;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.cvesters.notula.organisation.bdo.OrganisationUserRole;

@Converter(autoApply = true)
public class OrganisationUserRoleConverter
		implements AttributeConverter<OrganisationUserRole, Short> {

	@Override
	public Short convertToDatabaseColumn(final OrganisationUserRole role) {
		Objects.requireNonNull(role);

		return switch (role) {
			case ADMIN -> 0;
			case MEMBER -> 1;
		};
	}

	@Override
	public OrganisationUserRole convertToEntityAttribute(final Short value) {
		Objects.requireNonNull(value);

		return switch (value) {
			case 0 -> OrganisationUserRole.ADMIN;
			case 1 -> OrganisationUserRole.MEMBER;
			default -> throw new IllegalArgumentException();
		};
	}

}
