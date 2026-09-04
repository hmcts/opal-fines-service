package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureDisabledException;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.generated.http.api.TillsApi;
import uk.gov.hmcts.opal.generated.model.TillsResponse;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.TillService;
import uk.gov.hmcts.opal.service.opal.TillService.TillSearchCriteria;
import uk.gov.hmcts.opal.util.FeatureFlags;

@RestController
@Slf4j(topic = "opal.TillsApiController")
@RequiredArgsConstructor
public class TillsApiController implements TillsApi {

    private final DynamicConfigService dynamicConfigService;

    private final TillService tillService;

    @Override
    @FeatureToggle(feature = FeatureFlags.RELEASE_1C_PAYMENT,
        defaultValueProperty = FeatureFlags.RELEASE_1C_PAYMENT_ENABLED_PROPERTY)
    public ResponseEntity<TillsResponse> getTills(
        List<String> statuses, Boolean autoPayments, List<Short> businessUnitIds) {

        if (dynamicConfigService.isLegacyMode()) {
            log.debug(":GET:getTills: rejecting request because service is in legacy mode");
            throw new FeatureDisabledException("Get tills is only available in OPAL mode");
        }

        TillSearchCriteria searchCriteria = TillSearchCriteria.builder()
            .statuses(statuses)
            .autoPayments(autoPayments)
            .businessUnitIds(businessUnitIds)
            .build();

        log.debug(":GET:getTills: searchCriteria: {}", searchCriteria);

        return buildResponse(tillService.getTills(searchCriteria));
    }
}
