package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;
import uk.gov.hmcts.opal.service.legacy.LegacyMinorCreditorService;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

@RestController
@RequestMapping("/minor-creditor-accounts")
@Slf4j(topic = "opal.MinorCreditorController")
@Tag(name = "Minor Creditor Controller")
public class MinorCreditorController {

    private final LegacyMinorCreditorService minorCreditorService;

    public MinorCreditorController(LegacyMinorCreditorService minorCreditorService) {
        this.minorCreditorService = minorCreditorService;
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches MinorCreditors based upon criteria in request body")
    public ResponseEntity<MinorCreditorEntity> postMinorCreditorsSearch(
        @RequestBody MinorCreditorEntity criteria) {
        log.debug(":POST:postMinorCreditorsSearch: query: \n{}", criteria);

        MinorCreditorEntity response = minorCreditorService.searchMinorCreditors(criteria);

        return buildResponse(response);
    }

}
