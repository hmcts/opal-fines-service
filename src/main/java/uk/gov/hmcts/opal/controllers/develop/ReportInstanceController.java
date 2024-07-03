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
import uk.gov.hmcts.opal.dto.search.ReportInstanceSearchDto;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.service.ReportInstanceServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/report-instance")
@Slf4j(topic = "ReportInstanceController")
@Tag(name = "ReportInstance Controller")
public class ReportInstanceController {

    private final ReportInstanceServiceInterface reportInstanceService;

    public ReportInstanceController(@Qualifier("reportInstanceService")
                                    ReportInstanceServiceInterface reportInstanceService) {
        this.reportInstanceService = reportInstanceService;
    }

    @GetMapping(value = "/{reportInstanceId}")
    @Operation(summary = "Returns the ReportInstance for the given reportInstanceId.")
    public ResponseEntity<ReportInstanceEntity> getReportInstanceById(@PathVariable Long reportInstanceId) {

        log.info(":GET:getReportInstanceById: reportInstanceId: {}", reportInstanceId);

        ReportInstanceEntity response = reportInstanceService.getReportInstance(reportInstanceId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Report Instances based upon criteria in request body")
    public ResponseEntity<List<ReportInstanceEntity>> postReportInstancesSearch(@RequestBody
                                                                                    ReportInstanceSearchDto criteria) {
        log.info(":POST:postReportInstancesSearch: query: \n{}", criteria);

        List<ReportInstanceEntity> response = reportInstanceService.searchReportInstances(criteria);

        return buildResponse(response);
    }


}
