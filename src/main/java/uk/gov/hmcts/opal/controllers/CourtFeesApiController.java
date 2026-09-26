package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_PAYMENT;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_PAYMENT_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.generated.http.api.CourtFeesApi;
import uk.gov.hmcts.opal.generated.model.CourtFeesResponse;
import uk.gov.hmcts.opal.service.CourtFeesService;

@RestController
@Slf4j(topic = "opal.CourtFeesApiController")
@RequiredArgsConstructor
public class CourtFeesApiController implements CourtFeesApi {

    private final CourtFeesService courtFeesService;

    @Override
    @FeatureToggle(feature = RELEASE_1C_PAYMENT,
        defaultValueProperty = RELEASE_1C_PAYMENT_ENABLED_PROPERTY)
    public ResponseEntity<CourtFeesResponse> getCourtFees(Short businessUnitId) {
        CourtFeesResponse response = courtFeesService.getCourtFeesForBusinessUnit(businessUnitId);
        return buildResponse(response);
    }
}
