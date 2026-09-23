package uk.gov.hmcts.opal.controllers.till;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_PAYMENT;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_PAYMENT_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureDisabledException;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.generated.http.api.TillsApi;
import uk.gov.hmcts.opal.generated.model.TillsGetResponse;
import uk.gov.hmcts.opal.generated.model.TillsResponse;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.till.TillSearchService;
import uk.gov.hmcts.opal.service.opal.till.TillSearchService.TillSearchCriteria;
import uk.gov.hmcts.opal.service.opal.till.TillsService;

@RestController
@Slf4j(topic = "opal.TillsApiController")
@RequiredArgsConstructor
public class TillsApiController implements TillsApi {

    private final DynamicConfigService dynamicConfigService;

    private final TillSearchService tillSearchService;

    private final TillsService tillsService;

    @Override
    @FeatureToggle(feature = RELEASE_1C_PAYMENT,
        defaultValueProperty = RELEASE_1C_PAYMENT_ENABLED_PROPERTY)
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

        return buildResponse(tillSearchService.getTills(searchCriteria));
    }

    @Override
    @FeatureToggle(feature = RELEASE_1C_PAYMENT, defaultValueProperty = RELEASE_1C_PAYMENT_ENABLED_PROPERTY)
    public ResponseEntity<TillsGetResponse> getTill(Long id) {
        log.debug(":GET:getTill: id={}", id);
        return buildResponse(tillsService.getTill(id));
    }
}
