package com.cvesters.notula.organisation;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import com.cvesters.notula.organisation.bdo.OrganisationUserView;
import com.cvesters.notula.organisation.dao.OrganisationUserDao;

public interface OrganisationUserRepository
		extends Repository<OrganisationUserDao, Long> {

	List<OrganisationUserDao> findAllByUserId(long userId);

	@Query("""
		SELECT
			ou.id AS id,
			ou.organisationId AS organisationId,
			ou.userId AS userId,
			u.email AS email,
			ou.role AS role
		FROM organisation_users ou
		JOIN users u ON ou.userId = u.id
		WHERE ou.organisationId = :organisationId
		""")
	List<OrganisationUserView> findAllByOrganisationId(long organisationId);

	Optional<OrganisationUserDao> findByUserIdAndOrganisationId(long userId,
			long organisationId);

	OrganisationUserDao save(final OrganisationUserDao organisationUserDao);
}