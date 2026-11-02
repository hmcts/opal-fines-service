package uk.gov.hmcts.opal.service.reference.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.util.JsonPathUtil.createDocContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaLegacyEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaTypeLegacy;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaLegacyRepository;
import uk.gov.hmcts.opal.util.JsonPathUtil;
import uk.gov.hmcts.opal.util.JsonPathUtil.DocContext;

@ExtendWith(MockitoExtension.class)
public class LegacyDraftAccountReferenceValidationServiceTest {
    private static final short BUSINESS_UNIT_ID = 78;

    @Mock
    private OpalDraftAccountReferenceValidationService delegateService;

    @Mock
    private LocalJusticeAreaLegacyRepository localJusticeAreaLegacyRepository;

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
            verify(delegateService, times(1)).validateOriginator(eq(docContext), anyList(), any());
        }
    }

    @Test
    void validateOriginator_usesLegacyLocalJusticeAreaRepository() {
        Short localJusticeAreaId = 104;
        String localJusticeAreaName = "West London";
        DocContext docContext = mock(DocContext.class);
        List<String> failures = new ArrayList<>();
        LocalJusticeAreaLegacyEntity localJusticeArea = LocalJusticeAreaLegacyEntity.builder()
            .localJusticeAreaId(localJusticeAreaId)
            .ljaType(LocalJusticeAreaTypeLegacy.LJA)
            .name(localJusticeAreaName)
            .build();
        when(localJusticeAreaLegacyRepository.findById(localJusticeAreaId))
            .thenReturn(Optional.of(localJusticeArea));

        validationService.validateOriginator(docContext, failures);

        ArgumentCaptor<Function<Short, Optional<String>>> finderCaptor = ArgumentCaptor.captor();
        verify(delegateService).validateOriginator(eq(docContext), eq(failures), finderCaptor.capture());
        assertEquals(Optional.of(localJusticeAreaName), finderCaptor.getValue().apply(localJusticeAreaId));
    }
}
