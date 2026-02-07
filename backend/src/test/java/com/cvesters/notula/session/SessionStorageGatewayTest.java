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
import com.cvesters.notula.session.dao.SessionDao;

class SessionStorageGatewayTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final String HASHED_REFRESH_TOKEN = "hash";

	private final SessionRepository sessionRepository = mock();
	private final PasswordEncoder passwordEncoder = mock();

	private final SessionStorageGateway gateway = new SessionStorageGateway(
			sessionRepository, passwordEncoder);

	@Nested
	class FindById {

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

			final Optional<SessionInfo> result = gateway
					.findById(SESSION.getId());

			assertThat(result).isEmpty();
		}
	}

	@Nested
	class findByIdAndRefreshToken {

		@Test
		void found() {
			final SessionDao dao = mock();
			final SessionInfo bdo = mock();
			when(dao.getRefreshToken()).thenReturn(HASHED_REFRESH_TOKEN);
			when(dao.toBdo()).thenReturn(bdo);

			when(sessionRepository.findById(SESSION.getId()))
					.thenReturn(Optional.of(dao));

			when(passwordEncoder.matches(SESSION.getRefreshToken(),
					HASHED_REFRESH_TOKEN)).thenReturn(true);

			final Optional<SessionInfo> result = gateway
					.findByIdAndRefreshToken(SESSION.getId(),
							SESSION.getRefreshToken());

			assertThat(result).contains(bdo);
		}

		@Test
		void notFound() {
			when(sessionRepository.findById(SESSION.getId()))
					.thenReturn(Optional.empty());

			final Optional<SessionInfo> result = gateway
					.findByIdAndRefreshToken(SESSION.getId(),
							SESSION.getRefreshToken());

			assertThat(result).isEmpty();
		}

		@Test
		void tokenWrong() {
			final SessionDao dao = mock();

			when(sessionRepository.findById(SESSION.getId()))
					.thenReturn(Optional.of(dao));

			final Optional<SessionInfo> result = gateway
					.findByIdAndRefreshToken(SESSION.getId(), "Wrong");

			assertThat(result).isEmpty();
		}

		@Test
		void tokenNull() {
			final long sessionId = SESSION.getId();
			assertThatThrownBy(
					() -> gateway.findByIdAndRefreshToken(sessionId, null))
							.isInstanceOf(NullPointerException.class);
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

		private final SessionInfo update = mock();

		@Test
		void success() {
			when(update.getId()).thenReturn(SESSION.getId());

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
			order.verify(dao).update(update);
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

	@Nested
	class UpdateWithToken {

		private static final String REFRESH_TOKEN = "NEW_REFRESH_TOKEN";
		private final SessionInfo update = mock();

		@Test
		void success() {
			when(update.getId()).thenReturn(SESSION.getId());

			final SessionDao dao = mock();
			final SessionDao updated = mock();
			when(sessionRepository.findById(SESSION.getId()))
					.thenReturn(Optional.of(dao));
			when(sessionRepository.save(dao)).thenReturn(updated);

			when(passwordEncoder.encode(REFRESH_TOKEN))
					.thenReturn(HASHED_REFRESH_TOKEN);

			final SessionInfo updatedInfo = mock();
			when(updated.toBdo()).thenReturn(updatedInfo);

			final SessionInfo result = gateway.update(update, REFRESH_TOKEN);

			assertThat(result).isSameAs(updatedInfo);

			final InOrder order = inOrder(sessionRepository, dao);
			order.verify(dao).update(update, HASHED_REFRESH_TOKEN);
			order.verify(sessionRepository).save(dao);
		}

		@Test
		void notFound() {
			when(sessionRepository.findById(SESSION.getId()))
					.thenReturn(Optional.empty());

			assertThatThrownBy(() -> gateway.update(update, REFRESH_TOKEN))
					.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void updateNull() {
			assertThatThrownBy(() -> gateway.update(null, REFRESH_TOKEN))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void refreshTokenNull() {
			assertThatThrownBy(() -> gateway.update(update, null))
					.isInstanceOf(NullPointerException.class);
		}
	}

}
