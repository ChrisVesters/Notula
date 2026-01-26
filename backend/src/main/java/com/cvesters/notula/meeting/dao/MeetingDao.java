package com.cvesters.notula.meeting.dao;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import org.apache.commons.lang3.Validate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cvesters.notula.meeting.bdo.MeetingInfo;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Entity(name = "meetings")
public class MeetingDao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Setter(value = AccessLevel.PRIVATE)
	private Long id;

	@Column(name = "organisation_id", nullable = false, updatable = false)
	private long organisationId;

	@Column(nullable = false)
	private String name;

	public MeetingDao(final long organisationId, final String name) {
		Objects.requireNonNull(name);

		this.organisationId = organisationId;
		this.name = name;
	}

	public MeetingInfo toBdo() {
		Validate.validState(id != null);

		return new MeetingInfo(id, name);
	}
}
