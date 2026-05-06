package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildCreatedResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.generated.http.api.ReportInstancesApi;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceRequestReports;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;
import uk.gov.hmcts.opal.service.report.GenericReportService;

@RestController
@Slf4j(topic = "opal.ReportInstanceApiController")
@RequiredArgsConstructor
public class ReportInstancesApiController implements ReportInstancesApi {

    private final GenericReportService genericReportService;

    @Override
    public ResponseEntity<CreateReportInstanceResponseReports> createReportInstance(
        CreateReportInstanceRequestReports createReportInstanceRequestReports) {
        return buildCreatedResponse(genericReportService.addReportInstance(createReportInstanceRequestReports, true));
    }
}
