package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_PAYMENT;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_PAYMENT_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.generated.http.api.TillsApi;
import uk.gov.hmcts.opal.generated.model.TillsGetResponse;
import uk.gov.hmcts.opal.service.opal.GetTillService;

@RestController
@Slf4j(topic = "opal.TillsApiController")
@RequiredArgsConstructor
public class TillsApiController implements TillsApi {

    private final GetTillService getTillService;

    @Override
    @FeatureToggle(feature = RELEASE_1C_PAYMENT, defaultValueProperty = RELEASE_1C_PAYMENT_ENABLED_PROPERTY)
    public ResponseEntity<TillsGetResponse> getTill(Long id) {
        log.debug(":GET:getTill: id={}", id);
        return buildResponse(getTillService.getTill(id));
    }
}
