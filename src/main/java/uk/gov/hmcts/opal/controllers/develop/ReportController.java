package uk.gov.hmcts.opal.controllers.develop;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.search.ReportSearchDto;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.service.ReportServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/report")
@Slf4j(topic = "ReportController")
@Tag(name = "Report Controller")
public class ReportController {

    private final ReportServiceInterface reportService;

    public ReportController(@Qualifier("reportService") ReportServiceInterface reportService) {
        this.reportService = reportService;
    }

    @GetMapping(value = "/{reportId}")
    @Operation(summary = "Returns the Report for the given reportId.")
    public ResponseEntity<ReportEntity> getReportById(@PathVariable Long reportId) {

        log.info(":GET:getReportById: reportId: {}", reportId);

        ReportEntity response = reportService.getReport(reportId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Reports based upon criteria in request body")
    public ResponseEntity<List<ReportEntity>> postReportsSearch(@RequestBody ReportSearchDto criteria) {
        log.info(":POST:postReportsSearch: query: \n{}", criteria);

        List<ReportEntity> response = reportService.searchReports(criteria);

        return buildResponse(response);
    }


}
