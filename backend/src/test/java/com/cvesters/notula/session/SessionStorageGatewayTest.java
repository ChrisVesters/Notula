package com.cvesters.notula.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.session.bdo.SessionCreate;
import com.cvesters.notula.session.bdo.SessionInfo;
import com.cvesters.notula.session.bdo.SessionUpdate;
import com.cvesters.notula.session.dao.SessionDao;

class SessionStorageGatewayTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final String HASHED_REFRESH_TOKEN = "hash";

	private final SessionRepository sessionRepository = mock();
	private final PasswordEncoder passwordEncoder = mock();

	private final SessionStorageGateway gateway = new SessionStorageGateway(
			sessionRepository, passwordEncoder);

	@Nested
	class Find {

		@Test
		void found() {
			final SessionDao dao = mock();
			final SessionInfo bdo = mock();
			when(dao.toBdo()).thenReturn(bdo);

			when(sessionRepository.findById(SESSION.getId()))
					.thenReturn(Optional.of(dao));

			final Optional<SessionInfo> result = gateway
					.findById(SESSION.getId());

			assertThat(result).contains(bdo);
		}

		@Test
		void notFound() {
			when(sessionRepository.findById(SESSION.getId()))
					.thenReturn(Optional.empty());

			final Optional<SessionInfo> result = gateway.findById(SESSION.getId());

			assertThat(result).isEmpty();
		}
	}

	@Nested
	class Create {

		@Test
		void success() {
			final SessionDao created = mock();
			final SessionInfo bdo = mock();
			when(created.toBdo()).thenReturn(bdo);

			when(passwordEncoder.encode(SESSION.getRefreshToken()))
					.thenReturn(HASHED_REFRESH_TOKEN);

			when(sessionRepository.save(argThat(dao -> {
				assertThat(dao.getId()).isNull();
				assertThat(dao.getUserId())
						.isEqualTo(SESSION.getUser().getId());
				assertThat(dao.getRefreshToken())
						.isEqualTo(HASHED_REFRESH_TOKEN);
				assertThat(dao.getActiveUntil())
						.isEqualTo(SESSION.getActiveUntil());
				return true;
			}))).thenReturn(created);

			final SessionCreate action = mock();
			when(action.getUserId()).thenReturn(SESSION.getUser().getId());
			when(action.getRefreshToken())
					.thenReturn(SESSION.getRefreshToken());
			when(action.getActiveUntil()).thenReturn(SESSION.getActiveUntil());

			final SessionInfo sessionInfo = gateway.create(action);

			assertThat(sessionInfo).isEqualTo(bdo);
		}

		@Test
		void sessionInfoNull() {
			assertThatThrownBy(() -> gateway.create(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Update {

		private static final long ORGANIZATION_ID = 42L;
		private final SessionUpdate update = new SessionUpdate(SESSION.getId(),
				ORGANIZATION_ID);

		@Test
		void success() {
			final SessionDao dao = mock();
			final SessionDao updated = mock();
			when(sessionRepository.findById(SESSION.getId()))
					.thenReturn(Optional.of(dao));
			when(sessionRepository.save(dao)).thenReturn(updated);

			final SessionInfo updatedInfo = mock();
			when(updated.toBdo()).thenReturn(updatedInfo);

			final SessionInfo result = gateway.update(update);

			assertThat(result).isSameAs(updatedInfo);

			final InOrder order = inOrder(sessionRepository, dao);
			order.verify(dao).apply(update);
			order.verify(sessionRepository).save(dao);
		}

		@Test
		void notFound() {
			when(sessionRepository.findById(SESSION.getId()))
					.thenReturn(Optional.empty());

			assertThatThrownBy(() -> gateway.update(update))
					.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void updateNull() {
			assertThatThrownBy(() -> gateway.update(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
