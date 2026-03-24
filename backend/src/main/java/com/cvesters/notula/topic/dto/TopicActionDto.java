package com.cvesters.notula.topic.dto;

import jakarta.validation.constraints.NotBlank;

import com.cvesters.notula.topic.bdo.TopicAction;

public final class TopicActionDto {

	private TopicActionDto() {
	}

	public record Create(@NotBlank String name) {

		public TopicAction.Create toBdo() {
			return new TopicAction.Create(name);
		}

	}
	
}
