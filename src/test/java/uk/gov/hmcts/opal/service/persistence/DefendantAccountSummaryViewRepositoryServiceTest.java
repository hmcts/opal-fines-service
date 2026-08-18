package uk.gov.hmcts.opal.service.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountSummaryViewEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountSummaryViewRepository;

@ExtendWith(MockitoExtension.class)
class DefendantAccountSummaryViewRepositoryServiceTest {

    @Mock
    private DefendantAccountSummaryViewRepository repository;

    @InjectMocks
    private DefendantAccountSummaryViewRepositoryService service;

    @Test
    void getSummaryViewById_whenViewExists_returnsView() {
        // arrange
        Long defendantAccountId = 12L;
        DefendantAccountSummaryViewEntity view = DefendantAccountSummaryViewEntity.builder()
            .defendantAccountId(defendantAccountId)
            .build();

        when(repository.findById(defendantAccountId)).thenReturn(Optional.of(view));

        // act
        DefendantAccountSummaryViewEntity result = service.getSummaryViewById(defendantAccountId);

        // assert
        assertThat(result).isEqualTo(view);
        verify(repository).findById(defendantAccountId);
    }

    @Test
    void getSummaryViewById_whenViewMissing_throwsEntityNotFoundException() {
        // arrange
        Long defendantAccountId = 13L;
        when(repository.findById(defendantAccountId)).thenReturn(Optional.empty());

        // act / assert
        assertThatThrownBy(() -> service.getSummaryViewById(defendantAccountId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Defendant Account not found with id: " + defendantAccountId);

        verify(repository).findById(defendantAccountId);
    }
}
