package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.generated.http.api.ResultsApi;
import uk.gov.hmcts.opal.generated.model.GetResultByIdResponseResults;
import uk.gov.hmcts.opal.service.opal.ResultService;
import uk.gov.hmcts.opal.util.FeatureFlags;

@RestController
@Slf4j(topic = "opal.ResultsApiController")
@RequiredArgsConstructor
public class ResultsApiController implements ResultsApi {

    private final ResultService resultService;

    @Override
    @FeatureToggle(
        feature = FeatureFlags.RELEASE_1B,
        defaultValueProperty = FeatureFlags.RELEASE_1B_ENABLED_PROPERTY
    )
    public ResponseEntity<GetResultByIdResponseResults> getResultById(String id, Boolean includeWelsh) {

        log.debug(":GET:getResultById: resultId: {}, includeWelsh: {}", id, includeWelsh);

        return buildResponse(resultService.getResult(id, Boolean.TRUE.equals(includeWelsh)));
    }
}
