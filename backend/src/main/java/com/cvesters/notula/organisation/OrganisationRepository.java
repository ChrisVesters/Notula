package com.cvesters.notula.organisation;

import java.util.List;

import org.springframework.data.repository.Repository;

import com.cvesters.notula.organisation.dao.OrganisationDao;

public interface OrganisationRepository extends Repository<OrganisationDao, Long> {

	List<OrganisationDao> findAllById(final Iterable<Long> ids);

	OrganisationDao save(final OrganisationDao organisation);

}
