package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.ResultDto;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceDataResponse;
import uk.gov.hmcts.opal.service.opal.ResultService;

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
    @Operation(summary = "Returns the full ResultDto for the given resultId.")
    @Cacheable(value = "resultsCache", key = "#root.method.name + '_' + #resultId")
    public ResponseEntity<ResultDto> getResultById(@PathVariable String resultId) {

        log.debug(":GET:getResultById: resultId: {}", resultId);

        return buildResponse(resultService.getResult(resultId));
    }


    @GetMapping
    @Operation(summary = "Returns all results or results for the given resultIds.")
    public ResponseEntity<ResultReferenceDataResponse> getResults(
        @RequestParam(name = "result_ids") Optional<List<String>> resultIds,
        @RequestParam(name = "active", required = false) Boolean active,
        @RequestParam(name = "manual_enforcement_only", required = false) Boolean manualEnforcementOnly,
        @RequestParam(name = "generates_hearing", required = false) Boolean generatesHearing,
        @RequestParam(name = "enforcement", required = false) Boolean enforcement) {

        log.debug("GET:getResults: resultIds: {}", resultIds);

        return buildResponse(resultService.getResultsByIds(resultIds, active, manualEnforcementOnly, generatesHearing,
            enforcement));
    }

}
