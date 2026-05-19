package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.projection.DefendantAccountImpositionData;
import uk.gov.hmcts.opal.entity.projection.DefendantAccountVersionData;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionsResponseCommon;
import uk.gov.hmcts.opal.mapper.DefendantAccountImpositionMapper;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.ImpositionRepository;

@ExtendWith(MockitoExtension.class)
class OpalDefendantAccountImpositionsServiceTest {

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @Mock
    private ImpositionRepository impositionRepository;

    @Mock
    private DefendantAccountImpositionMapper defendantAccountImpositionMapper;

    @InjectMocks
    private OpalDefendantAccountService service;

    @Test
    void getDefendantAccountImpositions_returnsMappedPayloadWithAccountVersion() {
        Long defendantAccountId = 77L;
        List<DefendantAccountImpositionData> impositions = List.of();
        DefendantAccountImpositionsResponseCommon payload = new DefendantAccountImpositionsResponseCommon()
            .impositions(List.of());

        when(defendantAccountRepository.findVersionDataByDefendantAccountId(defendantAccountId))
            .thenReturn(Optional.of(new DefendantAccountVersionData(defendantAccountId, 12L)));
        when(impositionRepository.findDefendantAccountImpositionsByDefendantAccountId(defendantAccountId))
            .thenReturn(impositions);
        when(defendantAccountImpositionMapper.toResponse(impositions)).thenReturn(payload);

        var response = service.getDefendantAccountImpositions(defendantAccountId);

        assertSame(payload, response.getPayload());
        assertEquals(BigInteger.valueOf(12), response.getVersion());
        verify(defendantAccountRepository).findVersionDataByDefendantAccountId(defendantAccountId);
        verify(impositionRepository).findDefendantAccountImpositionsByDefendantAccountId(defendantAccountId);
        verify(defendantAccountImpositionMapper).toResponse(impositions);
    }

    @Test
    void getDefendantAccountImpositions_whenAccountDoesNotExist_throwsNotFound() {
        Long defendantAccountId = 77L;
        when(defendantAccountRepository.findVersionDataByDefendantAccountId(defendantAccountId))
            .thenReturn(Optional.empty());

        assertThrows(
            EntityNotFoundException.class,
            () -> service.getDefendantAccountImpositions(defendantAccountId)
        );

        verify(defendantAccountRepository).findVersionDataByDefendantAccountId(defendantAccountId);
        verifyNoInteractions(impositionRepository, defendantAccountImpositionMapper);
    }
}
