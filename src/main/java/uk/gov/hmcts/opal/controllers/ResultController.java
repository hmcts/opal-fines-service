package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceDataResults;
import uk.gov.hmcts.opal.entity.projection.ResultReferenceData;
import uk.gov.hmcts.opal.service.opal.ResultService;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/results")
@Slf4j(topic = "ResultController")
@Tag(name = "Result Controller")
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping(value = "/{resultId}")
    @Operation(summary = "Returns the Result for the given resultId.")
    public ResponseEntity<ResultReferenceData> getResultById(@PathVariable String resultId) {

        log.info(":GET:getResultById: resultId: {}", resultId);

        ResultReferenceData response = resultService.getResultReferenceData(resultId);

        return buildResponse(response);
    }

    @GetMapping
    @Operation(summary = "Returns all results all results or results for the given resultIds.")
    public ResponseEntity<ResultReferenceDataResults> getResults(
        @RequestParam(name = "result_ids", required = false) List<String> resultIds) {

        log.info(":GET:getResults: resultIds: {}", resultIds);

        List<ResultReferenceData> refData;

        if (resultIds == null || resultIds.isEmpty()) {
            refData = resultService.getAllResults();
        } else {
            refData = resultService.getResultsbyIds(resultIds);
        }

        return buildResponse(ResultReferenceDataResults.builder().refData(refData).build());

    }

}
