package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureDisabledException;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchReferenceNumberDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchRequestDefendantAccount;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.util.FeatureFlags;

@ExtendWith(MockitoExtension.class)
class DefendantAccountSearchRequestValidatorTest {

    @Mock
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Mock
    private FeatureToggleApi featureToggleApi;

    private DefendantAccountSearchRequestValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DefendantAccountSearchRequestValidator(jsonSchemaValidationService, featureToggleApi);
    }

    @Test
    void validateAndCheckFeature_validatesSchemaAndSkipsFlagLookupWhenConsolidationFalse() {
        // Arrange
        PostDefendantAccountSearchRequestDefendantAccount request = request(false);

        // Act / Assert
        assertDoesNotThrow(() -> validator.validateAndCheckFeature(request));

        // Assert
        verify(jsonSchemaValidationService).validateOrError(
            contains("\"consolidation_search\":false"),
            eq(SchemaPaths.POST_DEFENDANT_ACCOUNT_SEARCH_REQUEST)
        );
        verify(featureToggleApi, never()).isFeatureEnabledWithPropertyValueDefault(
            eq(FeatureFlags.RELEASE_1C),
            eq(FeatureFlags.RELEASE_1C_ENABLED_PROPERTY),
            eq(false)
        );
    }

    @Test
    void validateAndCheckFeature_checksFlagWhenConsolidationTrueAndFeatureEnabled() {
        // Arrange
        PostDefendantAccountSearchRequestDefendantAccount request = request(true);
        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
            FeatureFlags.RELEASE_1C,
            FeatureFlags.RELEASE_1C_ENABLED_PROPERTY,
            false
        )).thenReturn(true);

        // Act / Assert
        assertDoesNotThrow(() -> validator.validateAndCheckFeature(request));

        // Assert
        verify(jsonSchemaValidationService).validateOrError(
            contains("\"consolidation_search\":true"),
            eq(SchemaPaths.POST_DEFENDANT_ACCOUNT_SEARCH_REQUEST)
        );
        verify(featureToggleApi).isFeatureEnabledWithPropertyValueDefault(
            FeatureFlags.RELEASE_1C,
            FeatureFlags.RELEASE_1C_ENABLED_PROPERTY,
            false
        );
    }

    @Test
    void validateAndCheckFeature_throwsWhenConsolidationTrueAndFeatureDisabled() {
        // Arrange
        PostDefendantAccountSearchRequestDefendantAccount request = request(true);
        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
            FeatureFlags.RELEASE_1C,
            FeatureFlags.RELEASE_1C_ENABLED_PROPERTY,
            false
        )).thenReturn(false);

        // Act / Assert
        assertThrows(FeatureDisabledException.class, () -> validator.validateAndCheckFeature(request));

        // Assert
        verify(featureToggleApi).isFeatureEnabledWithPropertyValueDefault(
            FeatureFlags.RELEASE_1C,
            FeatureFlags.RELEASE_1C_ENABLED_PROPERTY,
            false
        );
    }

    private PostDefendantAccountSearchRequestDefendantAccount request(boolean consolidationSearch) {
        return PostDefendantAccountSearchRequestDefendantAccount.builder()
            .activeAccountsOnly(true)
            .businessUnitIds(List.of(78))
            .consolidationSearch(consolidationSearch)
            .referenceNumber(new DefendantAccountSearchReferenceNumberDefendantAccount()
                .organisation(false)
                .accountNumber("AC123")
                .prosecutorCaseReference(null))
            .build();
    }
}
