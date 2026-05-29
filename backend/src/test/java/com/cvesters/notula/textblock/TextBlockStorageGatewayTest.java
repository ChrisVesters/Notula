package com.cvesters.notula.textblock;

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

import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.textblock.bdo.TextBlockInfo;
import com.cvesters.notula.textblock.dao.TextBlockDao;

class TextBlockStorageGatewayTest {

	private final TextBlockRepository textBlockRepository = mock();

	private final TextBlockStorageGateway gateway = new TextBlockStorageGateway(
			textBlockRepository);

	@Nested
	class Find {

		private static final TestTextBlock TEXT_BLOCK = TestTextBlock.SPORER_PROJECT_BLOCKERS_FIRST;
		private static final TestBlock BLOCK = TEXT_BLOCK.getBlock();

		@Test
		void success() {
			final TextBlockDao textBlockDao = mock();
			final TextBlockInfo textBlockInfo = TEXT_BLOCK.info();
			when(textBlockDao.toBdo()).thenReturn(textBlockInfo);

			when(textBlockRepository.findByBlockId(BLOCK.getId()))
					.thenReturn(Optional.of(textBlockDao));

			final Optional<TextBlockInfo> result = gateway.find(BLOCK.getId());

			assertThat(result).contains(textBlockInfo);
		}

		@Test
		void notFound() {
			when(textBlockRepository.findByBlockId(BLOCK.getId()))
					.thenReturn(Optional.empty());

			final Optional<TextBlockInfo> result = gateway.find(BLOCK.getId());

			assertThat(result).isEmpty();
		}
	}

	@Nested
	class Update {

		private static final TestTextBlock TEXT_BLOCK = TestTextBlock.SPORER_PROJECT_BLOCKERS_FIRST;
		private static final TestBlock BLOCK = TEXT_BLOCK.getBlock();

		@Test
		void existing() {
			final TextBlockDao textBlockDao = mock();
			when(textBlockRepository.findByBlockId(BLOCK.getId()))
					.thenReturn(Optional.of(textBlockDao));

			final TextBlockDao updatedDao = mock();
			final TextBlockInfo updatedBdo = mock();
			when(updatedDao.toBdo()).thenReturn(updatedBdo);

			when(textBlockRepository.save(textBlockDao)).thenReturn(updatedDao);

			final TextBlockInfo update = TEXT_BLOCK.info();

			final TextBlockInfo result = gateway.update(update);

			assertThat(result).isEqualTo(updatedBdo);

			final InOrder inOrder = inOrder(textBlockDao, textBlockRepository);
			inOrder.verify(textBlockDao).update(update);
			inOrder.verify(textBlockRepository).save(textBlockDao);
		}

		@Test
		void nonExisting() {
			when(textBlockRepository.findByBlockId(BLOCK.getId()))
					.thenReturn(Optional.empty());

			final TextBlockDao updatedDao = mock();
			final TextBlockInfo updatedBdo = mock();
			when(updatedDao.toBdo()).thenReturn(updatedBdo);

			when(textBlockRepository.save(argThat(textBlock -> {
				assertThat(textBlock.getBlockId()).isEqualTo(BLOCK.getId());
				assertThat(textBlock.getContent()).isEqualTo(TEXT_BLOCK.getContent());
				return true;
			}))).thenReturn(updatedDao);

			final TextBlockInfo update = TEXT_BLOCK.info();

			final TextBlockInfo result = gateway.update(update);

			assertThat(result).isEqualTo(updatedBdo);
		}

		@Test
		void bdoNull() {
			assertThatThrownBy(() -> gateway.update(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
