package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import java.util.Optional;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.service.MinorCreditorService;
import uk.gov.hmcts.opal.service.opal.OpalCreditorAccountService;

@RestController
@RequestMapping("/minor-creditor-accounts")
@Slf4j(topic = "opal.MinorCreditorController")
@Tag(name = "Minor Creditor Controller")
public class MinorCreditorController {

    private final MinorCreditorService minorCreditorService;

    // Only used for the 'DELETE' endpoint, used in testing
    private final OpalCreditorAccountService opalCreditorAccountService;

    public MinorCreditorController(MinorCreditorService minorCreditorService,
                                   OpalCreditorAccountService opalCreditorAccountService) {
        this.minorCreditorService = minorCreditorService;
        this.opalCreditorAccountService = opalCreditorAccountService;
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

    @GetMapping(value = "{minorCreditorId}/at-a-glance")
    @Operation(summary = "Get Minor Creditor Account At A Glance")
    public ResponseEntity<GetMinorCreditorAccountAtAGlanceResponse> getMinorCreditorsAtAGlance(
        @PathVariable String minorCreditorId,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {
        log.debug(":GET:getMinorCreditorsAtAGlance: query: \n{}", minorCreditorId);

        GetMinorCreditorAccountAtAGlanceResponse response = minorCreditorService
            .getMinorCreditorAtAGlance(minorCreditorId, authHeaderValue);

        return buildResponse(response);
    }


    @Hidden
    @DeleteMapping(value = "/{minorCreditorId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Deletes the Minor Creditor for the given minorCreditorId.")
    @ConditionalOnProperty(prefix = "opal.testing-support-endpoints", name = "enabled", havingValue = "true")
    public ResponseEntity<String> deleteMinorCreditorById(
        @PathVariable Long minorCreditorId,
        @RequestHeader(value = "Authorization", required = false)  String authHeaderValue,
        @RequestHeader(value = "If-Match") String ifMatch,
        @RequestParam("ignore_missing") Optional<Boolean> ignoreMissing) {
        log.warn("TEST ENDPOINT: Request to delete creditor account {} and all associated data", minorCreditorId);

        // Note: This endpoint is used for testing only, so the 'If-Match' check is not actually used.
        boolean checkExisted = !(ignoreMissing.orElse(false));
        log.debug(":DELETE:deleteMinorCreditorById: Delete Draft Account: {}{}", minorCreditorId,
                  checkExisted ? "" : ", ignore if missing");

        return buildResponse(opalCreditorAccountService
                                 .deleteCreditorAccount((minorCreditorId), checkExisted, authHeaderValue));
    }
}
