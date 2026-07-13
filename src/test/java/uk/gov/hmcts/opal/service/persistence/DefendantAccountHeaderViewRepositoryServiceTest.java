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
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountHeaderViewEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountHeaderViewRepository;

@ExtendWith(MockitoExtension.class)
class DefendantAccountHeaderViewRepositoryServiceTest {

    @Mock
    private DefendantAccountHeaderViewRepository repository;

    @InjectMocks
    private DefendantAccountHeaderViewRepositoryService service;

    @Test
    void getHeaderViewById_whenViewExists_returnsView() {
        // arrange
        Long defendantAccountId = 21L;
        DefendantAccountHeaderViewEntity view = DefendantAccountHeaderViewEntity.builder()
            .defendantAccountId(defendantAccountId)
            .build();

        when(repository.findById(defendantAccountId)).thenReturn(Optional.of(view));

        // act
        DefendantAccountHeaderViewEntity result = service.getHeaderViewById(defendantAccountId);

        // assert
        assertThat(result).isEqualTo(view);
        verify(repository).findById(defendantAccountId);
    }

    @Test
    void getHeaderViewById_whenViewMissing_throwsEntityNotFoundException() {
        // arrange
        Long defendantAccountId = 22L;
        when(repository.findById(defendantAccountId)).thenReturn(Optional.empty());

        // act / assert
        assertThatThrownBy(() -> service.getHeaderViewById(defendantAccountId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Defendant Account not found with id: " + defendantAccountId);

        verify(repository).findById(defendantAccountId);
    }
}
