package com.cvesters.notula.organisation;

import java.util.List;

import org.springframework.data.repository.Repository;

import com.cvesters.notula.organisation.dao.OrganisationUserDao;

public interface OrganisationUserRepository extends Repository<OrganisationUserDao, Long> {

	List<OrganisationUserDao> findAllByUserId(long userId);

	OrganisationUserDao save(final OrganisationUserDao organisationUserDao);
}