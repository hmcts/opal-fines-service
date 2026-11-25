package uk.gov.hmcts.opal.service.reference.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.util.JsonPathUtil.createDocContext;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.ProsecutorLegacyEntity;
import uk.gov.hmcts.opal.entity.result.ResultLegacyEntity;
import uk.gov.hmcts.opal.repository.CourtLiteRepository;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.OffenceRepository;
import uk.gov.hmcts.opal.repository.ProsecutorLegacyRepository;
import uk.gov.hmcts.opal.repository.ResultRepository;
import uk.gov.hmcts.opal.util.JsonPathUtil;
import uk.gov.hmcts.opal.util.JsonPathUtil.DocContext;

@ExtendWith(MockitoExtension.class)
public class LegacyDraftAccountReferenceValidationServiceTest {
    private static final short BUSINESS_UNIT_ID = 78;

    @Mock
    private OpalDraftAccountReferenceValidationService delegateService;

    @Mock
    private CourtLiteRepository courtLiteRepository;

    @Mock
    private OffenceRepository offenceRepository;

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private CreditorAccountRepository creditorAccountRepository;

    @Mock
    private ProsecutorLegacyRepository prosecutorLegacyRepository;

    @InjectMocks
    private LegacyDraftAccountReferenceValidationService validationService;

    @Test
    void validateReferences_correctlyDelegatesToOpalService() {
        try (MockedStatic<JsonPathUtil> jsonPathUtil = mockStatic(JsonPathUtil.class)) {
            // Arrange
            String accountJson = "{}";
            DocContext docContext = mock(DocContext.class);
            jsonPathUtil.when(() -> createDocContext(accountJson, "DraftAccountReferenceValidationService"))
                .thenReturn(docContext);

            // Act
            validationService.validateReferences(BUSINESS_UNIT_ID, accountJson);

            // Assert
            verify(delegateService, times(1)).validateEnforcementCourt(eq(docContext), anyList());
            verify(delegateService, times(1)).validateOriginator(eq(docContext), anyList(), any());
        }
    }

    @Test
    void validateReferences_usesConfiguredResultRepositoryForImpositionsAndEnforcements() {
        when(resultRepository.findById("FINE")).thenReturn(Optional.of(ResultLegacyEntity.builder()
            .active(true)
            .imposition(true)
            .build()));
        when(resultRepository.findById("ENF")).thenReturn(Optional.of(ResultLegacyEntity.builder()
            .active(true)
            .enforcement(true)
            .build()));
        String accountJson = """
            {
              "offences": [{"impositions": [{"result_id": "FINE"}]}],
              "payment_terms": {"enforcements": [{"result_id": "ENF"}]}
            }
            """;

        assertDoesNotThrow(() -> validationService.validateReferences(BUSINESS_UNIT_ID, accountJson));

        verify(resultRepository).findById("FINE");
        verify(resultRepository).findById("ENF");
    }

    @SuppressWarnings("unchecked")
    @Test
    void validateOriginator_looksUpNumericKeyInLegacyRepository() {
        DocContext docContext = mock(DocContext.class);
        ProsecutorLegacyEntity prosecutor = ProsecutorLegacyEntity.builder()
            .prosecutorId((short) 9210)
            .name("Legacy Prosecutor")
            .build();
        when(delegateService.toShort("9210")).thenReturn((short) 9210);
        when(prosecutorLegacyRepository.findById((short) 9210)).thenReturn(Optional.of(prosecutor));
        ArgumentCaptor<Function<String, Optional<String>>> lookupCaptor = ArgumentCaptor.forClass(Function.class);

        validationService.validateOriginator(docContext, new ArrayList<>());

        verify(delegateService).validateOriginator(eq(docContext), anyList(), lookupCaptor.capture());
        assertEquals(Optional.of("Legacy Prosecutor"), lookupCaptor.getValue().apply("9210"));
        verify(delegateService).toShort("9210");
        verify(prosecutorLegacyRepository).findById((short) 9210);
    }

    @SuppressWarnings("unchecked")
    @Test
    void validateOriginator_doesNotQueryLegacyRepositoryForNonNumericKey() {
        DocContext docContext = mock(DocContext.class);
        ArgumentCaptor<Function<String, Optional<String>>> lookupCaptor = ArgumentCaptor.forClass(Function.class);
        when(delegateService.toShort("001X")).thenReturn(null);

        validationService.validateOriginator(docContext, new ArrayList<>());

        verify(delegateService).validateOriginator(eq(docContext), anyList(), lookupCaptor.capture());
        assertEquals(Optional.empty(), lookupCaptor.getValue().apply("001X"));
        verify(delegateService).toShort("001X");
        verifyNoInteractions(prosecutorLegacyRepository);
    }
}
