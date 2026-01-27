package uk.gov.hmcts.opal.service.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.repository.FixedPenaltyOffenceRepository;

@ExtendWith(MockitoExtension.class)
class FixedPenaltyOffenceRepositoryServiceTest {

    @Mock
    private FixedPenaltyOffenceRepository fixedPenaltyOffenceRepository;

    @InjectMocks
    private FixedPenaltyOffenceRepositoryService service;

    @Test
    void shouldReturnFixedPenaltyOffenceWhenFoundByDefendantAccountId() {
        // given
        Long defendantAccountId = 123L;
        FixedPenaltyOffenceEntity entity = mock(FixedPenaltyOffenceEntity.class);

        when(fixedPenaltyOffenceRepository.findByDefendantAccountId(defendantAccountId))
            .thenReturn(Optional.of(entity));

        // when
        FixedPenaltyOffenceEntity result =
            service.findByDefendantAccountId(defendantAccountId);

        // then
        assertThat(result).isSameAs(entity);
        verify(fixedPenaltyOffenceRepository)
            .findByDefendantAccountId(defendantAccountId);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenNotFoundByDefendantAccountId() {
        // given
        Long defendantAccountId = 999L;

        when(fixedPenaltyOffenceRepository.findByDefendantAccountId(defendantAccountId))
            .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
            service.findByDefendantAccountId(defendantAccountId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Fixed Penalty Offence not found for account: " + defendantAccountId);

        verify(fixedPenaltyOffenceRepository)
            .findByDefendantAccountId(defendantAccountId);
    }
}
