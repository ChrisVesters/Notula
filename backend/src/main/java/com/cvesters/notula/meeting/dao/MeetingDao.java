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

import com.cvesters.notula.meeting.bdo.MeetingInfo;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Entity(name = "meetings")
public class MeetingDao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "organisation_id", nullable = false, updatable = false)
	private long organisationId;

	@Column(nullable = false)
	private String name;

	public MeetingDao(final MeetingInfo bdo) {
		Objects.requireNonNull(bdo);

		this.organisationId = bdo.getOrganisationId();
		this.name = bdo.getName();
	}

	public MeetingInfo toBdo() {
		Validate.validState(id != null);

		return new MeetingInfo(id, organisationId, name);
	}
}
