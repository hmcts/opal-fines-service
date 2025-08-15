package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.service.MinorCreditorService;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

@RestController
@RequestMapping("/minor-creditor-accounts")
@Slf4j(topic = "opal.MinorCreditorController")
@Tag(name = "Minor Creditor Controller")
public class MinorCreditorController {

    private final MinorCreditorService minorCreditorService;

    public MinorCreditorController(MinorCreditorService minorCreditorService) {
        this.minorCreditorService = minorCreditorService;
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches MinorCreditors based upon criteria in request body")
    public ResponseEntity<PostMinorCreditorAccountsSearchResponse> postMinorCreditorsSearch(
        @RequestBody MinorCreditorSearch criteria,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {
        log.debug(":POST:postMinorCreditorsSearch: query: \n{}", criteria);

        PostMinorCreditorAccountsSearchResponse response = minorCreditorService
            .searchMinorCreditors(criteria, authHeaderValue);

        return buildResponse(response);
    }

}
