package uk.gov.hmcts.opal.service.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.repository.EnforcementRepository;

@ExtendWith(MockitoExtension.class)
class EnforcementRepositoryServiceTest {

    @Mock
    private EnforcementRepository enforcementRepository;

    @InjectMocks
    private EnforcementRepositoryService service;

    @Test
    void shouldReturnMostRecentEnforcementWhenRepositoryProvidesOne() {
        // given
        Long defendantAccountId = 123L;
        String lastEnforcement = "RESULT_1";
        EnforcementEntity enforcement = mock(EnforcementEntity.class);

        when(enforcementRepository
            .findFirstByDefendantAccountIdAndResultIdOrderByPostedDateDesc(defendantAccountId, lastEnforcement))
            .thenReturn(Optional.of(enforcement));

        // when
        Optional<EnforcementEntity> result = service.getEnforcementMostRecent(defendantAccountId, lastEnforcement);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(enforcement);
        verify(enforcementRepository).findFirstByDefendantAccountIdAndResultIdOrderByPostedDateDesc(defendantAccountId,
            lastEnforcement);
    }

    @Test
    void shouldReturnEmptyWhenRepositoryReturnsEmpty() {
        // given
        Long defendantAccountId = 456L;
        String lastEnforcement = "RESULT_NONE";

        when(enforcementRepository
            .findFirstByDefendantAccountIdAndResultIdOrderByPostedDateDesc(defendantAccountId, lastEnforcement))
            .thenReturn(Optional.empty());

        // when
        Optional<EnforcementEntity> result = service.getEnforcementMostRecent(defendantAccountId, lastEnforcement);

        // then
        assertThat(result).isEmpty();
        verify(enforcementRepository).findFirstByDefendantAccountIdAndResultIdOrderByPostedDateDesc(defendantAccountId,
            lastEnforcement);
    }

    @Test
    void shouldCallRepositoryWhenArgumentsAreNull() {
        // given
        Long defendantAccountId = null;
        String lastEnforcement = null;

        when(enforcementRepository
            .findFirstByDefendantAccountIdAndResultIdOrderByPostedDateDesc(defendantAccountId, lastEnforcement))
            .thenReturn(Optional.empty());

        // when
        Optional<EnforcementEntity> result = service.getEnforcementMostRecent(defendantAccountId, lastEnforcement);

        // then
        assertThat(result).isEmpty();
        verify(enforcementRepository).findFirstByDefendantAccountIdAndResultIdOrderByPostedDateDesc(defendantAccountId,
            lastEnforcement);
    }
}
