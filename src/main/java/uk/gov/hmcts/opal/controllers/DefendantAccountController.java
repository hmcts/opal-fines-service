package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.annotation.JsonSchemaValidated;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;

import uk.gov.hmcts.opal.dto.DefendantAccountResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;


import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

@RestController
@RequestMapping("/defendant-accounts")
@Slf4j(topic = "opal.DefendantAccountController")
@Tag(name = "Defendant Account Controller")
public class DefendantAccountController {

    private final DefendantAccountService defendantAccountService;

    public DefendantAccountController(DefendantAccountService defendantAccountService) {
        this.defendantAccountService = defendantAccountService;
    }

    @GetMapping(value = "/{defendantAccountId}/header-summary")
    @Operation(summary = "Get defendant account details by providing the defendant account summary")
    public ResponseEntity<DefendantAccountHeaderSummary> getHeaderSummary(
        @PathVariable Long defendantAccountId,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {

        log.debug(":GET:getHeaderSummary: for defendant id: {}", defendantAccountId);

        return buildResponse(
            defendantAccountService.getHeaderSummary(defendantAccountId, authHeaderValue));
    }

    @GetMapping(value = "/{defendantAccountId}/defendant-account-parties/{defendantAccountPartyId}")
    @Operation(summary = "Get details for a defendant account party")
    public ResponseEntity<GetDefendantAccountPartyResponse> getDefendantAccountParty(
        @PathVariable Long defendantAccountId,
        @PathVariable Long defendantAccountPartyId,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {

        log.debug(":GET:getDefendantAccountParty: for accountId={}, partyId={}", defendantAccountId,
            defendantAccountPartyId);

        GetDefendantAccountPartyResponse response =
            defendantAccountService.getDefendantAccountParty(defendantAccountId, defendantAccountPartyId,
                authHeaderValue);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches defendant accounts based upon criteria in request body")
    public ResponseEntity<DefendantAccountSearchResultsDto> postDefendantAccountSearch(
        @JsonSchemaValidated(schemaPath = SchemaPaths.POST_DEFENDANT_ACCOUNT_SEARCH_REQUEST)
            @RequestBody
           AccountSearchDto accountSearchDto,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {
        log.debug(":POST:postDefendantAccountSearch: query: \n{}", accountSearchDto.toPrettyJson());

        DefendantAccountSearchResultsDto response =
            defendantAccountService.searchDefendantAccounts(accountSearchDto, authHeaderValue);

        return buildResponse(response);
    }

    @GetMapping(value = "/{defendantAccountId}/payment-terms/latest")
    @Operation(summary = "Get defendant account details by providing the defendant account summary")
    public ResponseEntity<GetDefendantAccountPaymentTermsResponse> defendantAccountPaymentTerms(
        @PathVariable Long defendantAccountId,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {

        log.debug(":GET:DefendantAccountPaymentTerms: for defendant id: {}", defendantAccountId);

        return buildResponse(
            defendantAccountService.getPaymentTerms(defendantAccountId, authHeaderValue));
    }

    @GetMapping(value = "/{defendantAccountId}/at-a-glance")
    @Operation(summary = "Get At A Glance details for a given defendant account")
    public ResponseEntity<DefendantAccountAtAGlanceResponse> getAtAGlance(@PathVariable Long defendantAccountId,
              @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {

        return buildResponse(defendantAccountService.getAtAGlance(defendantAccountId, authHeaderValue));
    }

    @GetMapping(value = "/{defendantAccountId}/fixed-penalty")
    @Operation(summary = "Retrieve Fixed Penalty Offence details for a given Defendant Account")
    public ResponseEntity<GetDefendantAccountFixedPenaltyResponse> getDefendantAccountFixedPenalty(
        @PathVariable Long defendantAccountId,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {

        log.debug(":GET:getDefendantAccountFixedPenalty: for defendantAccountId={}", defendantAccountId);

        GetDefendantAccountFixedPenaltyResponse response =
            defendantAccountService.getDefendantAccountFixedPenalty(defendantAccountId, authHeaderValue);

        return buildResponse(response);
    }


    @PatchMapping(value = "/{defendantAccountId}", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update defendant account (comments, notes, enforcement details)")
    public ResponseEntity<DefendantAccountResponse> updateDefendantAccount(
        @PathVariable Long defendantAccountId,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue,
        @RequestHeader("Business-Unit-Id") String businessUnitId,
        @RequestHeader(value = "If-Match", required = false) String ifMatch,
        @JsonSchemaValidated(schemaPath = SchemaPaths.PATCH_UPDATE_DEFENDANT_ACCOUNT_REQUEST)
        @RequestBody UpdateDefendantAccountRequest request
    ) {
        log.debug(":PATCH:updateDefendantAccount: id={}", defendantAccountId);

        DefendantAccountResponse response = defendantAccountService.updateDefendantAccount(
            defendantAccountId, businessUnitId, request, ifMatch, authHeaderValue
        );

        return buildResponse(response);
    }

}
