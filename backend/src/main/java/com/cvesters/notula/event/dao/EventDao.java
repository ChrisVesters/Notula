package com.cvesters.notula.event.dao;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Entity(name = "events")
public class EventDao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "organisation_id", nullable = false, updatable = false)
	private long organisationId;

	@Column(name = "meeting_id", nullable = false, updatable = false)
	private long meetingId;

	@Column(nullable = false, updatable = false)
	private long revision;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, updatable = false)
	private String payload;

	public EventDao(final long organisationId, final long meetingId,
			final long revision, final String payload) {
		Objects.requireNonNull(payload);

		this.organisationId = organisationId;
		this.meetingId = meetingId;
		this.revision = revision;
		this.payload = payload;
	}
}
