package uk.gov.hmcts.opal.controllers;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_ENFORCEMENT_OPERATIONAL_REPORTING;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_ENFORCEMENT_OPERATIONAL_REPORTING_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildCreatedResponse;
import static uk.gov.hmcts.opal.util.HttpUtil.buildReportContentResponse;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;
import static uk.gov.hmcts.opal.util.ReportContentTypeUtil.resolveFileType;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.generated.http.api.ReportInstancesApi;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceRequestReports;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;
import uk.gov.hmcts.opal.generated.model.ReportInstanceListReportsInner;
import uk.gov.hmcts.opal.generated.model.ReportInstanceReports;
import uk.gov.hmcts.opal.service.report.FileType;
import uk.gov.hmcts.opal.service.report.GenericReportService;
import uk.gov.hmcts.opal.util.FeatureFlags;

@RestController
@Slf4j(topic = "opal.ReportInstancesApiController")
@RequiredArgsConstructor
public class ReportInstancesApiController implements ReportInstancesApi {

    private final GenericReportService genericReportService;
    private final HttpServletRequest request;

    @Override
    @FeatureToggle(
        feature = FeatureFlags.RELEASE_1C_ENFORCEMENT_OPERATIONAL_REPORTING,
        defaultValueProperty = FeatureFlags.RELEASE_1C_ENFORCEMENT_OPERATIONAL_REPORTING_ENABLED_PROPERTY
    )
    public ResponseEntity<List<ReportInstanceListReportsInner>> getReportInstances(
        LocalDate fromDate, LocalDate toDate, List<Integer> businessUnits, Integer userId, String reportId) {
        log.debug(":GET:getReportInstances: fromDate={}, toDate={}, businessUnits={}, userId={}, reportId={}",
            fromDate, toDate, businessUnits, userId, reportId);

        return buildResponse(genericReportService.searchReportInstances(fromDate, toDate, businessUnits,
            userId, reportId));
    }

    @Override
    @FeatureToggle(feature = RELEASE_1C_ENFORCEMENT_OPERATIONAL_REPORTING,
        defaultValueProperty = RELEASE_1C_ENFORCEMENT_OPERATIONAL_REPORTING_ENABLED_PROPERTY)
    public ResponseEntity<CreateReportInstanceResponseReports> createReportInstance(
        CreateReportInstanceRequestReports createReportInstanceRequestReports) {
        return buildCreatedResponse(genericReportService.addReportInstance(createReportInstanceRequestReports, true));
    }

    @FeatureToggle(feature = RELEASE_1C_ENFORCEMENT_OPERATIONAL_REPORTING,
        defaultValueProperty = RELEASE_1C_ENFORCEMENT_OPERATIONAL_REPORTING_ENABLED_PROPERTY)
    @Override
    public ResponseEntity<ReportInstanceReports> getReportInstance(Long id) {
        return buildResponse(genericReportService.getReportInstance(id));
    }

    @Override
    @FeatureToggle(
        feature = RELEASE_1C_ENFORCEMENT_OPERATIONAL_REPORTING,
        defaultValueProperty = RELEASE_1C_ENFORCEMENT_OPERATIONAL_REPORTING_ENABLED_PROPERTY
    )
    public ResponseEntity<Map<String, Object>> getReportInstanceContent(Long id) {
        FileType fileType = resolveFileType(request.getHeader(ACCEPT));
        if (fileType == null) {
            throw new ResponseStatusException(
                NOT_ACCEPTABLE,
                "The requested media type cannot be produced by the server"
            );
        }

        log.debug(":GET:getReportInstanceContent: for report instance id={}", id);

        Object content = genericReportService.getReportInstanceContent(id, fileType);
        return buildReportContentResponse(fileType, content);
    }

}
