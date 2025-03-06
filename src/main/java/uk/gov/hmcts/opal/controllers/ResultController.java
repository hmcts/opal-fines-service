package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
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
import java.util.Optional;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/results")
@Slf4j(topic = "opal.ResultController")
@Tag(name = "Result Controller")
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping(value = "/{resultId}")
    @Operation(summary = "Returns the Result for the given resultId.")
    @Cacheable(value = "resultsCache", key = "#root.method.name + '_' + #resultId")
    public ResponseEntity<ResultReferenceData> getResultById(@PathVariable Optional<String> resultId) {

        log.debug(":GET:getResultById: resultId: {}", resultId);

        ResultReferenceData response = resultId
            .filter(id -> !id.isBlank())
            .map(resultService::getResultReferenceData)
            .orElse(null);

        return buildResponse(response);
    }


    @GetMapping
    @Operation(summary = "Returns all results or results for the given resultIds.")
    @Cacheable(value = "resultsCache", key = "#root.method.name + '_' + #resultIds.orElse('ALL_RESULTS')")
    public ResponseEntity<ResultReferenceDataResults> getResults(
        @RequestParam(name = "result_ids") Optional<List<String>> resultIds) {

        log.debug("GET:getResults: resultIds: {}", resultIds);

        List<ResultReferenceData> refData = resultIds
            .filter(ids -> !ids.isEmpty())
            .map(resultService::getResultsByIds)
            .orElseGet(resultService::getAllResults);

        return buildResponse(ResultReferenceDataResults.builder().refData(refData).build());
    }

}
