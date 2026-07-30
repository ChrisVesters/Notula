package com.cvesters.notula.organisation.bdo;

// TODO: Not sure whether this mapping should be a BDO
// TODO: Maybe this should be a DAO, with an int instead of enum?
public interface OrganisationUserView {
	long getId();

	long getOrganisationId();

	long getUserId();

	String getEmail();

	OrganisationUserRole getRole();
}
