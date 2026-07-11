package com.cvesters.notula.credential;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cvesters.notula.credential.bdo.CredentialAction;
import com.cvesters.notula.credential.bdo.CredentialInfo;
import com.cvesters.notula.credential.dao.CredentialDao;

class CredentialStorageGatewayTest {

	private static final TestCredential CREDENTIAL = TestCredential.EDUARDO_CHRISTIANSEN;

	private final CredentialRepository credentialRepository = mock();
	private final PasswordEncoder passwordEncoder = mock();

	private final CredentialStorageGateway gateway = new CredentialStorageGateway(
			credentialRepository, passwordEncoder);

	@Nested
	class Create {

		@Test
		void success() {
			final String hashedPassword = "hash";
			when(passwordEncoder.encode(CREDENTIAL.getPassword().value()))
					.thenReturn(hashedPassword);

			final CredentialDao created = mock();
			final CredentialInfo bdo = mock();
			when(created.toBdo()).thenReturn(bdo);

			when(credentialRepository.save(argThat(dao -> {
				assertThat(dao.getId()).isNull();
				assertThat(dao.getUserId()).isEqualTo(CREDENTIAL.getUserId());
				assertThat(dao.getPassword()).isEqualTo(hashedPassword);
				return true;
			}))).thenReturn(created);

			final var action = new CredentialAction.Create(
					CREDENTIAL.getUserId(), CREDENTIAL.getPassword());

			final CredentialInfo info = gateway.create(action);

			assertThat(info).isEqualTo(bdo);
		}

		@Test
		void actionNull() {
			assertThatThrownBy(() -> gateway.create(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ExistsByUserId {

		@Test
		void found() {
			when(credentialRepository.existsByUserId(CREDENTIAL.getUserId()))
					.thenReturn(true);

			assertThat(gateway.existsByUserId(CREDENTIAL.getUserId())).isTrue();
		}

		@Test
		void notFound() {
			when(credentialRepository.existsByUserId(CREDENTIAL.getUserId()))
					.thenReturn(false);

			assertThat(gateway.existsByUserId(CREDENTIAL.getUserId()))
					.isFalse();
		}
	}

	@Nested
	class FindByLogin {

		@Test
		void found() {
			final CredentialInfo info = CREDENTIAL.info();
			final CredentialDao dao = mock();
			final String encodedPassword = "encoded";
			when(dao.toBdo()).thenReturn(info);
			when(dao.getPassword()).thenReturn(encodedPassword);

			when(credentialRepository.findByUserId(CREDENTIAL.getUserId()))
					.thenReturn(Optional.of(dao));

			final var login = new CredentialAction.Login(CREDENTIAL.getUserId(),
					CREDENTIAL.getPassword());
			when(passwordEncoder.matches(login.getPassword().value(),
					encodedPassword)).thenReturn(true);

			final Optional<CredentialInfo> found = gateway.findByLogin(login);

			assertThat(found).containsSame(info);
		}

		@Test
		void passwordIncorrect() {
			final CredentialInfo info = CREDENTIAL.info();
			final CredentialDao dao = mock();
			final String encodedPassword = "encoded";
			when(dao.toBdo()).thenReturn(info);
			when(dao.getPassword()).thenReturn(encodedPassword);

			when(credentialRepository.findByUserId(CREDENTIAL.getUserId()))
					.thenReturn(Optional.of(dao));

			final var login = new CredentialAction.Login(CREDENTIAL.getUserId(),
					CREDENTIAL.getPassword());
			when(passwordEncoder.matches(login.getPassword().value(),
					encodedPassword)).thenReturn(false);

			final Optional<CredentialInfo> found = gateway.findByLogin(login);

			assertThat(found).isEmpty();
		}

		@Test
		void notFound() {
			when(credentialRepository.findByUserId(CREDENTIAL.getUserId()))
					.thenReturn(Optional.empty());

			final var login = new CredentialAction.Login(CREDENTIAL.getUserId(),
					CREDENTIAL.getPassword());
			final Optional<CredentialInfo> found = gateway.findByLogin(login);

			assertThat(found).isEmpty();
		}

		@Test
		void loginNull() {
			assertThatThrownBy(() -> gateway.findByLogin(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
