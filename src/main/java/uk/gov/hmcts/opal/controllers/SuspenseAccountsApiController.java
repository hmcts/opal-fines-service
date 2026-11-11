package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.generated.http.api.SuspenseAccountsApi;
import uk.gov.hmcts.opal.generated.model.SuspenseItemHistoryCommonStrict;
import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;
import uk.gov.hmcts.opal.service.SuspenseAccountsHistoryService;
import uk.gov.hmcts.opal.service.SuspenseAccountsSummaryService;

@RestController
@Slf4j(topic = "opal.SuspenseAccountsApiController")
@RequiredArgsConstructor
public class SuspenseAccountsApiController implements SuspenseAccountsApi {

    private final SuspenseAccountsSummaryService suspenseAccountsSummaryService;
    private final SuspenseAccountsHistoryService suspenseAccountsHistoryService;

    @FeatureToggle(feature = RELEASE_1C, defaultValueProperty = RELEASE_1C_ENABLED_PROPERTY)
    @Override
    public ResponseEntity<SuspenseItemSummaryCommonStrict> getSuspenseItemSummary(Short businessUnitId,
        Long suspenseItemNumber) {

        log.debug("GET: getSuspenseItemSummary: businessUnitId: {}, suspenseItemNumber: {}",
            businessUnitId, suspenseItemNumber);

        SuspenseItemSummaryCommonStrict response = suspenseAccountsSummaryService
            .getSuspenseAccountsSummary(businessUnitId, suspenseItemNumber);

        if (response == null) {
            throw new EntityNotFoundException("Suspense item summary not found");
        }

        return buildResponse(response);
    }

    @Override
    @FeatureToggle(feature = RELEASE_1C, defaultValueProperty = RELEASE_1C_ENABLED_PROPERTY)
    public ResponseEntity<SuspenseItemHistoryCommonStrict> getSuspenseItemHistory(Long businessUnitId,
        Long suspenseItemNumber) {

        log.debug("GET: getSuspenseItemHistory: businessUnitId: {}, suspenseItemNumber: {}",
            businessUnitId, suspenseItemNumber);

        return buildResponse(suspenseAccountsHistoryService
            .getSuspenseItemHistory(businessUnitId, suspenseItemNumber));
    }

}
