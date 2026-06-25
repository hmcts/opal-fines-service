package uk.gov.hmcts.opal.service;

import tools.jackson.core.JacksonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureDisabledException;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchRequestDefendantAccount;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.util.FeatureFlags;

@Service
@RequiredArgsConstructor
public class DefendantAccountSearchRequestValidator {

    private final JsonSchemaValidationService jsonSchemaValidationService;
    private final FeatureToggleApi featureToggleApi;

    public void validateAndCheckFeature(PostDefendantAccountSearchRequestDefendantAccount request) {
        jsonSchemaValidationService.validateOrError(toJson(request), SchemaPaths.POST_DEFENDANT_ACCOUNT_SEARCH_REQUEST);

        if (Boolean.TRUE.equals(request.getConsolidationSearch())
            && !featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
                FeatureFlags.RELEASE_1C,
                FeatureFlags.RELEASE_1C_ENABLED_PROPERTY,
                false
            )) {
            throw new FeatureDisabledException(
                "Feature release-1c is not enabled for defendant account consolidated search");
        }
    }

    private String toJson(PostDefendantAccountSearchRequestDefendantAccount request) {
        try {
            return ToJsonString.getObjectMapper().writeValueAsString(request);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Unable to serialise defendant account search request", e);
        }
    }
}
