package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_PAYMENT;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_PAYMENT_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.generated.http.api.BusinessUnitOutstandingAutoPaymentApi;
import uk.gov.hmcts.opal.generated.model.BusinessUnitsOutstandingAutoPaymentResponse;
import uk.gov.hmcts.opal.service.opal.OutstandingAutoPaymentService;

@RestController
@Slf4j(topic = "opal.OutstandingAutoPaymentController")
@RequiredArgsConstructor
public class OutstandingAutoPaymentController implements BusinessUnitOutstandingAutoPaymentApi {

    private final OutstandingAutoPaymentService service;

    @Override
    @FeatureToggle(feature = RELEASE_1C_PAYMENT, defaultValueProperty = RELEASE_1C_PAYMENT_ENABLED_PROPERTY)
    public ResponseEntity<BusinessUnitsOutstandingAutoPaymentResponse> getBusinessUnitOutstandingAutoPaymentCount() {
        log.debug(":GET:getBusinessUnitOutstandingAutoPaymentCount");

        return buildResponse(service.getOutstandingAutoPaymentCount());
    }
}
