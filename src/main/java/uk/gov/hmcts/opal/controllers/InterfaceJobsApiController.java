package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.generated.http.api.InterfaceJobsApi;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsSummaryResponse;
import uk.gov.hmcts.opal.service.opal.InterfaceJobService.InterfaceJobSearchCriteria;
import uk.gov.hmcts.opal.service.opal.InterfaceJobService;
import uk.gov.hmcts.opal.util.FeatureFlags;

@RestController
@Slf4j(topic = "opal.InterfaceJobsApiController")
@RequiredArgsConstructor
public class InterfaceJobsApiController implements InterfaceJobsApi {

    private final InterfaceJobService interfaceJobService;

    @Override
    @FeatureToggle(feature = FeatureFlags.RELEASE_1C_PAYMENT,
        defaultValueProperty = FeatureFlags.RELEASE_1C_PAYMENT_ENABLED_PROPERTY)
    public ResponseEntity<InterfaceJobsSummaryResponse> getInterfaceJobsSummary(
        List<Short> businessUnitIds, List<String> statuses, LocalDateTime completedDateFrom,
        LocalDateTime completedDateTo, String interfaceName) {

        InterfaceJobSearchCriteria searchCriteria = InterfaceJobSearchCriteria.builder()
            .businessUnitIds(businessUnitIds)
            .statuses(statuses)
            .completedDateFrom(completedDateFrom)
            .completedDateTo(completedDateTo)
            .interfaceName(interfaceName)
            .build();

        log.debug(":GET:getInterfaceJobsSummary: searchCriteria: {}", searchCriteria);

        return buildResponse(interfaceJobService.getSummary(searchCriteria));
    }

}
