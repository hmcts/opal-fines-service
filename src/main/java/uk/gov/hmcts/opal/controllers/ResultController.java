package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureDisabledException;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.dto.ResultDto;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceDataResponse;
import uk.gov.hmcts.opal.service.opal.ResultService;
import uk.gov.hmcts.opal.util.FeatureFlags;

@RestController
@RequestMapping("/results")
@Slf4j(topic = "opal.ResultController")
@Tag(name = "Result Controller")
public class ResultController {

    private static final Set<String> RELEASE_1B_FILTER_PARAMETERS = Set.of(
        "active",
        "manual_enforcement_only",
        "generates_hearing",
        "enforcement",
        "enforcement_override"
    );

    private final ResultService resultService;
    private final FeatureToggleApi featureToggleApi;

    public ResultController(ResultService resultService, FeatureToggleApi featureToggleApi) {
        this.resultService = resultService;
        this.featureToggleApi = featureToggleApi;
    }

    @GetMapping(value = "/{resultId}")
    @Operation(summary = "Returns the full ResultDto for the given resultId.")
    //DONE
    // @Cacheable(value = "resultsCache", key = "#root.method.name + '_' + #resultId")
    @FeatureToggle(
        feature = FeatureFlags.RELEASE_1B,
        defaultValueProperty = FeatureFlags.RELEASE_1B_ENABLED_PROPERTY
    )
    public ResponseEntity<ResultDto> getResultById(@PathVariable String resultId) {

        log.debug(":GET:getResultById: resultId: {}", resultId);

        return buildResponse(resultService.getResult(resultId));
    }


    @GetMapping
    @Operation(summary = "Returns all results or results for the given resultIds.")
    @FeatureToggle(feature = FeatureFlags.RELEASE_1A, defaultValueProperty = FeatureFlags.RELEASE_1A_ENABLED_PROPERTY)
    public ResponseEntity<ResultReferenceDataResponse> getResults(
        @RequestParam MultiValueMap<String, String> requestParams,
        @RequestParam(name = "result_ids") Optional<List<String>> resultIds,
        @RequestParam(name = "active", required = false) Boolean active,
        @RequestParam(name = "manual_enforcement_only", required = false) Boolean manualEnforcementOnly,
        @RequestParam(name = "generates_hearing", required = false) Boolean generatesHearing,
        @RequestParam(name = "enforcement", required = false) Boolean enforcement,
        @RequestParam(name = "enforcement_override", required = false) Boolean enforcementOverride) {

        log.debug("GET:getResults: resultIds: {}", resultIds);

        rejectFilteringWhenDisabled(requestParams);

        return buildResponse(resultService.getResultsByIds(resultIds, active, manualEnforcementOnly, generatesHearing,
            enforcement, enforcementOverride));
    }

    private void rejectFilteringWhenDisabled(MultiValueMap<String, String> requestParams) {
        if (filteringRequested(requestParams) && !featureToggleApi.isFeatureEnabled(FeatureFlags.RELEASE_1B)) {
            throw new FeatureDisabledException("Feature release-1b is not enabled for results filtering");
        }
    }

    private boolean filteringRequested(MultiValueMap<String, String> requestParams) {
        return RELEASE_1B_FILTER_PARAMETERS.stream().anyMatch(requestParams::containsKey);
    }

}
