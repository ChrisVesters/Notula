package com.cvesters.notula.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import com.cvesters.notula.TestContainerConfig;

@Sql({ "/db/users.sql", "/db/organisations.sql", "/db/blueprints.sql" })
@DataJpaTest
@Import(TestContainerConfig.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class UserRepositoryTest {

	@PersistenceContext
	private EntityManager entityManager;
}
