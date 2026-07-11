package com.cvesters.notula.credential;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.exception.DuplicateEntityException;
import com.cvesters.notula.credential.bdo.CredentialAction;
import com.cvesters.notula.credential.bdo.CredentialInfo;

class CredentialServiceTest {

	private static final TestCredential CREDENTIAL = TestCredential.EDUARDO_CHRISTIANSEN;

	private final CredentialStorageGateway credentialStorageGateway = mock();

	private final CredentialService service = new CredentialService(
			credentialStorageGateway);

	@Nested
	class Create {

		@Test
		void success() {
			final CredentialInfo info = CREDENTIAL.info();
			final CredentialAction.Create action = CREDENTIAL.create();

			when(credentialStorageGateway.existsByUserId(action.getUserId()))
					.thenReturn(false);

			when(credentialStorageGateway.create(action)).thenReturn(info);

			assertThat(service.create(action)).isEqualTo(info);
		}

		@Test
		void duplicate() {
			final CredentialAction.Create action = CREDENTIAL.create();

			when(credentialStorageGateway.existsByUserId(action.getUserId()))
					.thenReturn(true);

			assertThatThrownBy(() -> service.create(action))
					.isInstanceOf(DuplicateEntityException.class);
		}

		@Test
		void actionNull() {
			assertThatThrownBy(() -> service.create(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ExistsLogin {

		@Test
		void exists() {
			final CredentialInfo info = CREDENTIAL.info();
			final CredentialAction.Login login = CREDENTIAL.login();
			when(credentialStorageGateway.findByLogin(login))
					.thenReturn(Optional.of(info));

			assertThat(service.existsLogin(login)).isTrue();
		}

		@Test
		void notExists() {
			final CredentialAction.Login login = CREDENTIAL.login();

			when(credentialStorageGateway.findByLogin(login))
					.thenReturn(Optional.empty());

			assertThat(service.existsLogin(login)).isFalse();
		}

		@Test
		void loginNull() {
			assertThatThrownBy(() -> service.existsLogin(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
