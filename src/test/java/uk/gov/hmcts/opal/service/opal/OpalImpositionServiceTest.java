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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.projection.DefendantAccountImpositionData;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionsResponseCommon;
import uk.gov.hmcts.opal.mapper.DefendantAccountImpositionMapper;
import uk.gov.hmcts.opal.repository.ImpositionRepository;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;

@ExtendWith(MockitoExtension.class)
class OpalImpositionServiceTest {

    @Mock
    private DefendantAccountRepositoryService defendantAccountRepositoryService;

    @Mock
    private ImpositionRepository impositionRepository;

    @Mock
    private DefendantAccountImpositionMapper defendantAccountImpositionMapper;

    @InjectMocks
    private OpalImpositionService service;

    @Test
    void getDefendantAccountImpositions_returnsMappedPayloadWithAccountVersion() {
        Long defendantAccountId = 77L;
        List<DefendantAccountImpositionData> impositions = List.of();
        DefendantAccountImpositionsResponseCommon payload = new DefendantAccountImpositionsResponseCommon()
            .impositions(List.of());

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(defendantAccountId)
            .versionNumber(12L)
            .build();
        when(defendantAccountRepositoryService.findById(defendantAccountId)).thenReturn(account);
        when(impositionRepository.findDefendantAccountImpositionsByDefendantAccountId(defendantAccountId))
            .thenReturn(impositions);
        when(defendantAccountImpositionMapper.toResponse(impositions)).thenReturn(payload);

        var response = service.getImpositions(defendantAccountId);

        assertSame(payload, response.getPayload());
        assertEquals(BigInteger.valueOf(12), response.getVersion());
        verify(defendantAccountRepositoryService).findById(defendantAccountId);
        verify(impositionRepository).findDefendantAccountImpositionsByDefendantAccountId(defendantAccountId);
        verify(defendantAccountImpositionMapper).toResponse(impositions);
    }

    @Test
    void getDefendantAccountImpositions_whenAccountDoesNotExist_throwsNotFound() {
        Long defendantAccountId = 77L;
        EntityNotFoundException expectedException = new EntityNotFoundException(
            "Defendant Account not found with id: " + defendantAccountId);
        when(defendantAccountRepositoryService.findById(defendantAccountId)).thenThrow(expectedException);

        assertThrows(
            EntityNotFoundException.class,
            () -> service.getImpositions(defendantAccountId)
        );

        verify(defendantAccountRepositoryService).findById(defendantAccountId);
        verifyNoInteractions(impositionRepository, defendantAccountImpositionMapper);
    }
}
