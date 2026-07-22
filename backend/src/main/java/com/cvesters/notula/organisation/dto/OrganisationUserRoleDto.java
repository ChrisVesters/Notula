package com.cvesters.notula.organisation.dto;

import java.util.Objects;

import com.cvesters.notula.common.exception.InvalidActionException;
import com.cvesters.notula.organisation.bdo.OrganisationUserRole;
import com.fasterxml.jackson.annotation.JsonValue;

public record OrganisationUserRoleDto(@JsonValue String role) {

	public OrganisationUserRoleDto {
		Objects.requireNonNull(role);
	}

	public OrganisationUserRoleDto(final OrganisationUserRole role) {
		Objects.requireNonNull(role);

		final String value = switch (role) {
			case ADMIN -> "ADMIN";
			case MEMBER -> "MEMBER";
		};

		this(value);
	}

	public OrganisationUserRole toBdo() {
		return switch (role) {
			case "ADMIN" -> OrganisationUserRole.ADMIN;
			case "MEMBER" -> OrganisationUserRole.MEMBER;
			// TODO: proper validation exception
			default -> throw new InvalidActionException();
		};
	}
}
