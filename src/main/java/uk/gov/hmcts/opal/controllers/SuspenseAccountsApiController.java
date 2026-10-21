package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_ENABLED_PROPERTY;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.generated.http.api.SuspenseAccountsApi;
import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;
import uk.gov.hmcts.opal.service.SuspenseAccountsSummaryService;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

@RestController
@Slf4j(topic = "opal.SuspenseAccountsApiController")
@RequiredArgsConstructor
public class SuspenseAccountsApiController implements SuspenseAccountsApi {

    private final SuspenseAccountsSummaryService suspenseAccountsSummaryService;

    @FeatureToggle(feature = RELEASE_1C, defaultValueProperty = RELEASE_1C_ENABLED_PROPERTY)
    @Override
    public ResponseEntity<SuspenseItemSummaryCommonStrict> getSuspenseItemSummary(Long businessUnitId,
        Long suspenseItemNumber) {

        log.debug("GET: getSuspenseItemSummary: businessUnitId: {}, suspenseItemNumber: {}",
            businessUnitId, suspenseItemNumber);

        return buildResponse(suspenseAccountsSummaryService
            .getSuspenseAccountsSummary(businessUnitId, suspenseItemNumber));
    }

}
