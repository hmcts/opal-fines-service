package uk.gov.hmcts.opal.service.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.result.ResultOpalEntity;
import uk.gov.hmcts.opal.repository.ResultOpalRepository;

@ExtendWith(MockitoExtension.class)
class ResultOpalRepositoryServiceTest {

    @Mock
    private ResultOpalRepository resultRepository;

    @InjectMocks
    private ResultOpalRepositoryService resultRepositoryService;

    @Test
    void shouldReturnResultWhenIdIsProvidedAndResultExists() {
        // given
        String resultId = "RESULT_123";
        ResultOpalEntity lite = ResultOpalEntity.builder().resultId(resultId).resultTitle("Result title").build();

        when(resultRepository.findById(resultId)).thenReturn(Optional.of(lite));

        // when
        Optional<ResultOpalEntity> result = resultRepositoryService.getResultById(resultId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getResultId()).isEqualTo(resultId);
        assertThat(result.get().getResultTitle()).isEqualTo("Result title");
        verify(resultRepository).findById(resultId);
    }

    @Test
    void shouldReturnEmptyWhenIdIsProvidedButResultDoesNotExist() {
        // given
        String resultId = "RESULT_404";

        when(resultRepository.findById(resultId)).thenReturn(Optional.empty());

        // when
        Optional<ResultOpalEntity> result = resultRepositoryService.getResultById(resultId);

        // then
        assertThat(result).isEmpty();
        verify(resultRepository).findById(resultId);
    }

    @Test
    void shouldReturnEmptyAndNotCallRepositoryWhenIdIsNull() {
        // when
        Optional<ResultOpalEntity> result = resultRepositoryService.getResultById(null);

        // then
        assertThat(result).isEmpty();
        verifyNoInteractions(resultRepository);
    }
}
