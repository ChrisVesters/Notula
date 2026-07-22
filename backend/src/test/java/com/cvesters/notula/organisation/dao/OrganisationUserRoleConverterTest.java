package com.cvesters.notula.organisation.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.cvesters.notula.organisation.bdo.OrganisationUserRole;

class OrganisationUserRoleConverterTest {

	public final OrganisationUserRoleConverter converter = new OrganisationUserRoleConverter();

	@Nested
	class ConvertToDatabaseColumn {

		@Test
		void admin() {
			assertThat(converter
					.convertToDatabaseColumn(OrganisationUserRole.ADMIN))
							.isZero();
		}

		@Test
		void member() {
			assertThat(converter
					.convertToDatabaseColumn(OrganisationUserRole.MEMBER))
							.isOne();
		}

		@Test
		void roleNull() {
			assertThatThrownBy(() -> converter.convertToDatabaseColumn(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ConvertToEntityAttribute {

		@Test
		void admin() {
			assertThat(converter.convertToEntityAttribute((short) 0))
					.isEqualTo(OrganisationUserRole.ADMIN);
		}

		@Test
		void member() {
			assertThat(converter.convertToEntityAttribute((short) 1))
					.isEqualTo(OrganisationUserRole.MEMBER);
		}

		@Test
		void valueNull() {
			assertThatThrownBy(() -> converter.convertToEntityAttribute(null))
					.isInstanceOf(NullPointerException.class);
		}

		@ParameterizedTest
		@ValueSource(ints = { -1, 10 })
		void valueInvalid(final int value) {
			assertThatThrownBy(
					() -> converter.convertToEntityAttribute((short) value))
							.isInstanceOf(IllegalArgumentException.class);
		}
	}
}
