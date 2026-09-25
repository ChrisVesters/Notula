package com.cvesters.notula.event.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Splice;
import com.cvesters.notula.event.bdo.BlockMutation;
import com.cvesters.notula.event.bdo.MeetingMutation;
import com.cvesters.notula.event.bdo.TextBlockMutation;
import com.cvesters.notula.event.bdo.TopicMutation;
import com.cvesters.notula.meeting.dto.TextEditDto;

class MutationDtoTest {

	@Nested
	class Of {

		@Test
		void meeting() {
			final var dto = MutationDto.of(new MeetingMutation.Remove());

			assertThat(dto).isEqualTo(new MeetingMutationDto.Remove());
		}

		@Test
		void topic() {
			final var dto = MutationDto.of(new TopicMutation.Remove(32L));

			assertThat(dto).isEqualTo(new TopicMutationDto.Remove(32L));
		}

		@Test
		void block() {
			final var dto = MutationDto.of(new BlockMutation.Remove(9L));

			assertThat(dto).isEqualTo(new BlockMutationDto.Remove(9L));
		}

		@Test
		void textBlock() {
			final var mutation = new TextBlockMutation.Edit(9L,
					new Splice(4, 12, "Updated"));

			final var dto = MutationDto.of(mutation);

			final var edit = new TextEditDto(4, 12, "Updated");
			assertThat(dto).isEqualTo(new TextBlockMutationDto.Edit(9L, edit));
		}

		@Test
		void mutationNull() {
			assertThatThrownBy(() -> MutationDto.of(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
