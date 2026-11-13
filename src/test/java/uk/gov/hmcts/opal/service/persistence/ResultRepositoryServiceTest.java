package uk.gov.hmcts.opal.service.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.result.ResultLegacyEntity;
import uk.gov.hmcts.opal.repository.ResultRepository;

@ExtendWith(MockitoExtension.class)
class ResultRepositoryServiceTest {

    @Mock
    private ResultRepository resultRepository;

    @InjectMocks
    private ResultRepositoryService resultRepositoryService;

    @Test
    void shouldReturnResultWhenIdIsProvidedAndResultExists() {
        // given
        String resultId = "RESULT_123";
        ResultLegacyEntity lite = mock(ResultLegacyEntity.class);

        when(resultRepository.findById(resultId)).thenReturn(Optional.of(lite));

        // when
        Optional<ResultLegacyEntity> result = resultRepositoryService.getResultById(resultId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(lite);
        verify(resultRepository).findById(resultId);
    }

    @Test
    void shouldReturnEmptyWhenIdIsProvidedButResultDoesNotExist() {
        // given
        String resultId = "RESULT_404";

        when(resultRepository.findById(resultId)).thenReturn(Optional.empty());

        // when
        Optional<ResultLegacyEntity> result = resultRepositoryService.getResultById(resultId);

        // then
        assertThat(result).isEmpty();
        verify(resultRepository).findById(resultId);
    }

    @Test
    void shouldReturnEmptyAndNotCallRepositoryWhenIdIsNull() {
        // when
        Optional<ResultLegacyEntity> result = resultRepositoryService.getResultById(null);

        // then
        assertThat(result).isEmpty();
        verifyNoInteractions(resultRepository);
    }
}
