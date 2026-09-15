package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureDisabledException;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.generated.http.api.InterfaceJobsApi;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateRequest;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateResponse;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsProcessRequest;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsProcessedFileSummaryResponse;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsSummaryResponse;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.InterfaceJobService;
import uk.gov.hmcts.opal.service.opal.InterfaceJobProcessedFileSummaryService;
import uk.gov.hmcts.opal.service.opal.InterfaceJobService.InterfaceJobSearchCriteria;
import uk.gov.hmcts.opal.util.FeatureFlags;

@RestController
@Slf4j(topic = "opal.InterfaceJobsApiController")
@RequiredArgsConstructor
public class InterfaceJobsApiController implements InterfaceJobsApi {

    private final DynamicConfigService dynamicConfigService;
    private final InterfaceJobService interfaceJobService;

    private final InterfaceJobProcessedFileSummaryService processedFileSummaryService;

    @Override
    @FeatureToggle(feature = FeatureFlags.RELEASE_1C_PAYMENT,
        defaultValueProperty = FeatureFlags.RELEASE_1C_PAYMENT_ENABLED_PROPERTY)
    public ResponseEntity<Void> processInterfaceJobs(InterfaceJobsProcessRequest request) {
        if (dynamicConfigService.isLegacyMode()) {
            log.debug(":POST:processInterfaceJobs: rejecting request because service is in legacy mode");
            throw new FeatureDisabledException("Interface job processing is only available in OPAL mode");
        }

        interfaceJobService.process(request);
        return ResponseEntity.ok().build();
    }

    @Override
    @FeatureToggle(feature = FeatureFlags.RELEASE_1C_PAYMENT,
        defaultValueProperty = FeatureFlags.RELEASE_1C_PAYMENT_ENABLED_PROPERTY)
    public ResponseEntity<InterfaceJobsCreateResponse> postInterfaceJobs(
        InterfaceJobsCreateRequest request) {

        return buildResponse(interfaceJobService.create(request));
    }

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

    @Override
    @FeatureToggle(feature = FeatureFlags.RELEASE_1C_PAYMENT,
        defaultValueProperty = FeatureFlags.RELEASE_1C_PAYMENT_ENABLED_PROPERTY)
    public ResponseEntity<InterfaceJobsProcessedFileSummaryResponse> getInterfaceJobProcessedFileSummary(Long id) {

        log.debug(":GET:getInterfaceJobProcessedFileSummary: id={}", id);

        if (dynamicConfigService.isLegacyMode()) {
            log.debug(":GET:getInterfaceJobProcessedFileSummary: rejecting request as service is in legacy mode");
            throw new FeatureDisabledException("Processed file summary is only available in OPAL mode");
        }
        try {
            return buildResponse(processedFileSummaryService.getProcessedFileSummary(id));
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Unable to retrieve processed file summary", e);
        }
    }

}
