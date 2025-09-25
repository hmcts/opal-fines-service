package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import uk.gov.hmcts.opal.dto.response.SearchDataResponse;
import uk.gov.hmcts.opal.dto.search.AmendmentSearchDto;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity;
import uk.gov.hmcts.opal.service.opal.AmendmentService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/amendments")
@Slf4j(topic = "opal.AmendmentController")
@ConditionalOnProperty(prefix = "opal.testing-support-endpoints", name = "enabled", havingValue = "true")
public class AmendmentController {

    private final AmendmentService amendmentService;

    public AmendmentController(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    @GetMapping(value = "/{amendmentId}")
    @Operation(summary = "Returns the Amendment for the given DB primary key id.")
    public ResponseEntity<AmendmentEntity> getAmendmentById(@PathVariable Long amendmentId) {

        log.debug(":GET:getAmendmentById: amendmentId: {}", amendmentId);
        return buildResponse(amendmentService.getAmendmentById(amendmentId));
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Amendments based upon criteria in request body")
    public ResponseEntity<SearchDataResponse<AmendmentEntity>> postAmendmentsSearch(
        @RequestBody AmendmentSearchDto criteria) {

        log.debug(":POST:postAmendmentsSearch: query: \n{}", criteria);
        return buildResponse(amendmentService.searchAmendments(criteria));
    }
}
