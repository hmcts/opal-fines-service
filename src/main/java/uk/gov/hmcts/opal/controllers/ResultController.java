package uk.gov.hmcts.opal.controllers;

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
import uk.gov.hmcts.opal.dto.reference.ResultReferenceDataResults;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.ResultEntity;
import uk.gov.hmcts.opal.entity.projection.ResultReferenceData;
import uk.gov.hmcts.opal.service.ResultServiceInterface;
import uk.gov.hmcts.opal.service.opal.ResultService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/api/result")
@Slf4j(topic = "ResultController")
@Tag(name = "Result Controller")
public class ResultController {

    private final ResultServiceInterface resultService;

    private final ResultService opalResultService;

    public ResultController(@Qualifier("resultServiceProxy") ResultServiceInterface resultService,
                            ResultService opalResultService) {
        this.resultService = resultService;
        this.opalResultService = opalResultService;
    }

    @GetMapping(value = "/{resultId}")
    @Operation(summary = "Returns the Result for the given resultId.")
    public ResponseEntity<ResultEntity> getResultById(@PathVariable Long resultId) {

        log.info(":GET:getResultById: resultId: {}", resultId);

        ResultEntity response = resultService.getResult(resultId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Results based upon criteria in request body")
    public ResponseEntity<List<ResultEntity>> postResultsSearch(@RequestBody ResultSearchDto criteria) {
        log.info(":POST:postResultsSearch: query: \n{}", criteria);

        List<ResultEntity> response = resultService.searchResults(criteria);

        return buildResponse(response);
    }

    @GetMapping(value = {"/ref-data", "/ref-data/", "/ref-data/{filter}"})
    @Operation(summary = "Returns MajorCreditors as reference data with an optional filter applied")
    public ResponseEntity<ResultReferenceDataResults> getResultRefData(
        @PathVariable Optional<String> filter) {
        log.info(":GET:getMajorCreditorRefData: filter string: {}", filter);

        List<ResultReferenceData> refData = opalResultService.getReferenceData(filter);

        log.info(":GET:getMajorCreditorRefData: major creditor reference data count: {}", refData.size());
        return ResponseEntity.ok(ResultReferenceDataResults.builder().refData(refData).build());
    }
}
