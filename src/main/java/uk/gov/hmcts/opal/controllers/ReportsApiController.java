package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureFlags;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.generated.http.api.ReportsApi;
import uk.gov.hmcts.opal.generated.model.ReportReports;
import uk.gov.hmcts.opal.service.ReportService;

@RestController
@Slf4j(topic = "opal.ReportsApiController")
@RequiredArgsConstructor
public class ReportsApiController implements ReportsApi {

    private final ReportService reportService;

    @FeatureToggle(feature = FeatureFlags.RELEASE_1C_ENFORCEMENT_OPERATIONAL_REPORTING,
        defaultValueProperty = FeatureFlags.RELEASE_1C_ENFORCEMENT_OPERATIONAL_REPORTING_DEFAULT_VALUE_PROPERTY)
    @Override
    public ResponseEntity<ReportReports> getReport(String id) {

        log.debug(":GET:getReport: for report id={}", id);

        return buildResponse(reportService.getReport(id));
    }
}
