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
import uk.gov.hmcts.opal.dto.search.ReportEntrySearchDto;
import uk.gov.hmcts.opal.entity.ReportEntryEntity;
import uk.gov.hmcts.opal.service.ReportEntryServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/report-entries")
@Slf4j(topic = "ReportEntryController")
@Tag(name = "ReportEntry Controller")
public class ReportEntryController {

    private final ReportEntryServiceInterface reportEntryService;

    public ReportEntryController(@Qualifier("reportEntryService") ReportEntryServiceInterface reportEntryService) {
        this.reportEntryService = reportEntryService;
    }

    @GetMapping(value = "/{reportEntryId}")
    @Operation(summary = "Returns the ReportEntry for the given reportEntryId.")
    public ResponseEntity<ReportEntryEntity> getReportEntryById(@PathVariable Long reportEntryId) {

        log.info(":GET:getReportEntryById: reportEntryId: {}", reportEntryId);

        ReportEntryEntity response = reportEntryService.getReportEntry(reportEntryId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Report Entries based upon criteria in request body")
    public ResponseEntity<List<ReportEntryEntity>> postReportEntriesSearch(@RequestBody ReportEntrySearchDto criteria) {
        log.info(":POST:postReportEntriesSearch: query: \n{}", criteria);

        List<ReportEntryEntity> response = reportEntryService.searchReportEntries(criteria);

        return buildResponse(response);
    }


}
