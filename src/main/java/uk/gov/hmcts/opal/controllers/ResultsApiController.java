package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureDisabledException;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.generated.http.api.ResultsApi;
import uk.gov.hmcts.opal.generated.model.GetResultByIdResponseResults;
import uk.gov.hmcts.opal.generated.model.GetResultsResponseResults;
import uk.gov.hmcts.opal.service.opal.ResultService;
import uk.gov.hmcts.opal.util.FeatureFlags;

@RestController
@Slf4j(topic = "opal.ResultsApiController")
@RequiredArgsConstructor
public class ResultsApiController implements ResultsApi {

    private static final List<String> RELEASE_1B_FILTER_PARAMETERS = List.of(
        "active",
        "manual_enforcement_only",
        "generates_hearing",
        "enforcement",
        "enforcement_override"
    );

    private final ResultService resultService;
    private final FeatureToggleApi featureToggleApi;
    private final HttpServletRequest request;

    @Override
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<GetResultByIdResponseResults> getResultById(String id, Boolean includeWelsh) {

        log.debug(":GET:getResultById: resultId: {}, includeWelsh: {}", id, includeWelsh);

        return buildResponse(resultService.getResult(id, Boolean.TRUE.equals(includeWelsh)));
    }

    @Override
    @FeatureToggle(
        feature = FeatureFlags.RELEASE_1A,
        defaultValueProperty = FeatureFlags.RELEASE_1A_ENABLED_PROPERTY
    )
    public ResponseEntity<GetResultsResponseResults> getResults(
        List<String> resultIds,
        Boolean active,
        Boolean manualEnforcementOnly,
        Boolean generatesHearing,
        Boolean enforcement,
        Boolean enforcementOverride) {

        log.debug(":GET:getResults: resultIds={}", resultIds);

        rejectFilteringWhenDisabled();

        return buildResponse(resultService.getResultsByIds(resultIds, active, manualEnforcementOnly, generatesHearing,
            enforcement, enforcementOverride));
    }

    private void rejectFilteringWhenDisabled() {
        boolean filteringRequested = RELEASE_1B_FILTER_PARAMETERS.stream()
            .anyMatch(request.getParameterMap()::containsKey);
        if (filteringRequested && !featureToggleApi.isFeatureEnabled(FeatureFlags.RELEASE_1B)) {
            throw new FeatureDisabledException("Feature release-1b is not enabled for results filtering");
        }
    }
}
