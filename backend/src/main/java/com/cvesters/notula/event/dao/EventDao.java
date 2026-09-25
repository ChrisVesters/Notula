package com.cvesters.notula.event.dao;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import org.apache.commons.lang3.Validate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.cvesters.notula.event.bdo.EventInfo;
import com.cvesters.notula.event.dto.MutationDto;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Entity(name = "events")
public class EventDao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "meeting_id", nullable = false, updatable = false)
	private long meetingId;

	@Column(nullable = false, updatable = false)
	private long revision;

	@Column(name = "user_id", nullable = false, updatable = false)
	private long userId;

	@Column(name = "client_id", updatable = false)
	private UUID clientId;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, updatable = false)
	private MutationDto mutation;

	public EventDao(final EventInfo event) {
		Objects.requireNonNull(event);

		this.meetingId = event.getMeetingId();
		this.revision = event.getRevision();
		this.userId = event.getUserId();
		this.clientId = event.getClientId();
		this.mutation = MutationDto.of(event.getMutation());
	}

	public EventInfo toBdo() {
		Validate.validState(id != null);

		return new EventInfo(id, meetingId, revision, userId, clientId,
				mutation.toBdo());
	}
}
