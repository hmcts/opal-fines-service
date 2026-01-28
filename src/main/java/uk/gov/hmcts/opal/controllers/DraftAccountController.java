package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.annotation.CheckAcceptHeader;
import uk.gov.hmcts.opal.annotation.JsonSchemaValidated;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.DraftAccountResponseDto;
import uk.gov.hmcts.opal.dto.DraftAccountsResponseDto;
import uk.gov.hmcts.opal.dto.ReplaceDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.UpdateDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.service.DraftAccountService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.opal.util.HttpUtil.buildCreatedResponse;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

@RestController
@RequestMapping("/draft-accounts")
@Slf4j(topic = "opal.DraftAccountController")
@Tag(name = "DraftAccount Controller")
public class DraftAccountController {

    private final DraftAccountService draftAccountService;

    public DraftAccountController(DraftAccountService draftAccountService) {
        this.draftAccountService = draftAccountService;
    }

    @GetMapping(value = "/{draftAccountId}")
    @CheckAcceptHeader
    @Operation(summary = "Returns the Draft Account for the given draftAccountId.")
    public ResponseEntity<DraftAccountResponseDto> getDraftAccountById(
        @PathVariable Long draftAccountId,
        @RequestHeader(value = "Authorization", required = false)  String authHeaderValue) {

        log.debug(":GET:getDraftAccountById: draftAccountId: {}", draftAccountId);

        return buildResponse(draftAccountService.getDraftAccount(draftAccountId, authHeaderValue));
    }

    @GetMapping()
    @CheckAcceptHeader
    @Operation(summary = "Returns a collection of draft accounts summaries for the given user.")
    public ResponseEntity<DraftAccountsResponseDto> getDraftAccountSummaries(
        @RequestParam(value = "business_unit") Optional<List<Short>> optionalBusinessUnitIds,
        @RequestParam(value = "status") Optional<List<DraftAccountStatus>> optionalStatus,
        @RequestParam(value = "submitted_by") Optional<List<String>> optionalSubmittedBys,
        @RequestParam(value = "not_submitted_by") Optional<List<String>> optionalNotSubmittedBys,
        @RequestParam(value = "account_status_date_from") Optional<LocalDate> accountStatusDateFrom,
        @RequestParam(value = "account_status_date_to") Optional<LocalDate> accountStatusDateTo,
        @RequestHeader(value = "Authorization", required = false)  String authHeaderValue) {

        log.debug(":GET:getDraftAccountSummaries:");

        return buildResponse(
            draftAccountService.getDraftAccounts(optionalBusinessUnitIds, optionalStatus, optionalSubmittedBys,
                         optionalNotSubmittedBys, accountStatusDateFrom, accountStatusDateTo, authHeaderValue));
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Draft Accounts based upon criteria in request body")
    public ResponseEntity<List<DraftAccountResponseDto>> postDraftAccountsSearch(
        @RequestBody DraftAccountSearchDto criteria,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {

        log.debug(":POST:postDraftAccountsSearch: query: \n{}", criteria);

        return buildResponse(draftAccountService.searchDraftAccounts(criteria, authHeaderValue));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Creates a Draft Account Entity in the DB based upon data in request body")
    @CheckAcceptHeader
    public ResponseEntity<DraftAccountResponseDto> postDraftAccount(
        @JsonSchemaValidated(schemaPath = SchemaPaths.DRAFT_ACCOUNT + "/addDraftAccountRequest.json")
        @RequestBody AddDraftAccountRequestDto dto,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {

        log.debug(":POST:postDraftAccount: creating a new draft account entity: \n{}", dto.toPrettyJson());

        return buildCreatedResponse(draftAccountService.submitDraftAccount(dto, authHeaderValue));
    }

    @Hidden
    @DeleteMapping(value = "/{draftAccountId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Deletes the Draft Account for the given draftAccountId.")
    @ConditionalOnProperty(prefix = "opal.testing-support-endpoints", name = "enabled", havingValue = "true")
    public ResponseEntity<String> deleteDraftAccountById(
        @PathVariable Long draftAccountId,
        @RequestHeader(value = "Authorization", required = false)  String authHeaderValue,
        @RequestHeader(value = "If-Match", required = false) String ifMatch,
        @RequestParam("ignore_missing") Optional<Boolean> ignoreMissing) {

        // Note: This endpoint is used for testing only, so the 'If-Match' check is not actually used.
        boolean checkExisted = !(ignoreMissing.orElse(false));

        log.debug(":DELETE:deleteDraftAccountById: Delete Draft Account: {}{}", draftAccountId,
                  checkExisted ? "" : ", ignore if missing");

        return buildResponse(draftAccountService.deleteDraftAccount((draftAccountId), checkExisted, authHeaderValue));

    }

    @PutMapping(value = "/{draftAccountId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Replaces an existing Draft Account Entity in the DB with data in request body")
    @CheckAcceptHeader
    public ResponseEntity<DraftAccountResponseDto> putDraftAccount(
        @PathVariable Long draftAccountId,
        @JsonSchemaValidated(schemaPath = SchemaPaths.DRAFT_ACCOUNT + "/replaceDraftAccountRequest.json")
        @RequestBody ReplaceDraftAccountRequestDto dto,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue,
        @RequestHeader(value = "If-Match") String ifMatch) {

        log.debug(":PUT:putDraftAccount: replacing draft account '{}' with: \n{}", draftAccountId, dto.toPrettyJson());

        return buildResponse(draftAccountService.replaceDraftAccount(draftAccountId, dto, authHeaderValue, ifMatch));
    }

    @PatchMapping(value = "/{draftAccountId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updates an existing Draft Account Entity in the DB with data in request body")
    @CheckAcceptHeader
    public ResponseEntity<DraftAccountResponseDto> patchDraftAccount(
        @PathVariable Long draftAccountId,
        @JsonSchemaValidated(schemaPath = SchemaPaths.DRAFT_ACCOUNT + "/updateDraftAccountRequest.json")
        @RequestBody UpdateDraftAccountRequestDto dto,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue,
        @RequestHeader(value = "If-Match") String ifMatch) {

        log.info(":PATCH:patchDraftAccount: updating draft account entity: {}", draftAccountId);

        return buildResponse(draftAccountService.updateDraftAccount(draftAccountId, dto, authHeaderValue, ifMatch));
    }
}
