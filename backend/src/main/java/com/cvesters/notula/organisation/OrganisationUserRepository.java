package com.cvesters.notula.organisation;

import org.springframework.data.repository.Repository;

import com.cvesters.notula.organisation.dao.OrganisationUserDao;

public interface OrganisationUserRepository extends Repository<OrganisationUserDao, Long> {

	// Optional<OrganisationUserDao> findByOrganisationIdAndUserId(long organisationId, long userId);

	OrganisationUserDao save(final OrganisationUserDao organisationUserDao);
}