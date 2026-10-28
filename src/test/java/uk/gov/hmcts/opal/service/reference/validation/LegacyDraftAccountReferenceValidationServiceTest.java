package uk.gov.hmcts.opal.service.reference.validation;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.opal.util.JsonPathUtil.createDocContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.util.JsonPathUtil;
import uk.gov.hmcts.opal.util.JsonPathUtil.DocContext;

@ExtendWith(MockitoExtension.class)
public class LegacyDraftAccountReferenceValidationServiceTest {
    private static final short BUSINESS_UNIT_ID = 78;

    @Mock
    private OpalDraftAccountReferenceValidationService delegateService;

    @InjectMocks
    private LegacyDraftAccountReferenceValidationService validationService;

    @Test
    void validateReferences_correctlyDelegatesToOpalService() {
        try(MockedStatic<JsonPathUtil> jsonPathUtil = mockStatic(JsonPathUtil.class)) {
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
            verify(delegateService, times(1)).validateOriginator(eq(docContext), anyList());
        }
    }
}
