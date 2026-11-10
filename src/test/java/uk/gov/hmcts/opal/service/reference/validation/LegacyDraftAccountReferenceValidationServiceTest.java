package uk.gov.hmcts.opal.service.reference.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.util.JsonPathUtil.createDocContext;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.ProsecutorLegacyEntity;
import uk.gov.hmcts.opal.repository.ProsecutorLegacyRepository;
import uk.gov.hmcts.opal.util.JsonPathUtil;
import uk.gov.hmcts.opal.util.JsonPathUtil.DocContext;

@ExtendWith(MockitoExtension.class)
public class LegacyDraftAccountReferenceValidationServiceTest {
    private static final short BUSINESS_UNIT_ID = 78;

    @Mock
    private OpalDraftAccountReferenceValidationService delegateService;

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
            verify(delegateService, times(1)).validateOffences(eq(BUSINESS_UNIT_ID), eq(docContext), anyList());
            verify(delegateService, times(1)).validatePaymentTermsEnforcements(eq(docContext), anyList(), anyMap());
        }
    }

    @Test
    void validateReferences_usesLegacyProsecutorId() {
        when(prosecutorLegacyRepository.findById(32010L)).thenReturn(Optional.of(
            ProsecutorLegacyEntity.builder()
                .prosecutorId(32010L)
                .name("Draft Account Validation Prosecutor")
                .build()));

        assertDoesNotThrow(() -> validationService.validateReferences(BUSINESS_UNIT_ID, """
            {
              "account_type": "Fixed Penalty",
              "originator_type": "FP",
              "originator_id": 32010,
              "originator_name": "Draft Account Validation Prosecutor"
            }
            """));

        verify(prosecutorLegacyRepository).findById(32010L);
    }
}
