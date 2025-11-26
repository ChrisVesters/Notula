package com.cvesters.notula.organisation;

import org.springframework.data.repository.Repository;

import com.cvesters.notula.organisation.dao.OrganisationDao;

public interface OrganisationRepository extends Repository<OrganisationDao, Long> {

	OrganisationDao save(final OrganisationDao organisation);

}
