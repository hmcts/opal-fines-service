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
import uk.gov.hmcts.opal.annotation.CheckAcceptHeader;
import uk.gov.hmcts.opal.authorisation.aspect.PermissionNotAllowedException;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.DraftAccountResponseDto;
import uk.gov.hmcts.opal.dto.DraftAccountSummaryDto;
import uk.gov.hmcts.opal.dto.DraftAccountsResponseDto;
import uk.gov.hmcts.opal.dto.ReplaceDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.UpdateDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.DraftAccountStatus;
import uk.gov.hmcts.opal.service.opal.DraftAccountService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.opal.util.DateTimeUtils.toOffsetDateTime;
import static uk.gov.hmcts.opal.util.HttpUtil.buildCreatedResponse;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/draft-accounts")
@Slf4j(topic = "DraftAccountController")
@Tag(name = "DraftAccount Controller")
public class DraftAccountController {

    public static final String ADD_DRAFT_ACCOUNT_REQUEST_JSON = "addDraftAccountRequest.json";
    public static final String REPLACE_DRAFT_ACCOUNT_REQUEST_JSON = "replaceDraftAccountRequest.json";
    public static final String UPDATE_DRAFT_ACCOUNT_REQUEST_JSON = "updateDraftAccountRequest.json";
    public static final String ACCOUNT_DELETED_MESSAGE_FORMAT = """
        { "message": "Draft Account '%s' deleted"}""";

    private final DraftAccountService draftAccountService;

    private final UserStateService userStateService;

    private final JsonSchemaValidationService jsonSchemaValidationService;

    public DraftAccountController(UserStateService userStateService, DraftAccountService draftAccountService,
                                  JsonSchemaValidationService jsonSchemaValidationService) {
        this.draftAccountService = draftAccountService;
        this.userStateService = userStateService;
        this.jsonSchemaValidationService = jsonSchemaValidationService;
    }

    @GetMapping(value = "/{draftAccountId}")
    @CheckAcceptHeader
    @Operation(summary = "Returns the Draft Account for the given draftAccountId.")
    public ResponseEntity<DraftAccountResponseDto> getDraftAccountById(
        @PathVariable Long draftAccountId,
        @RequestHeader(value = "Authorization", required = false)  String authHeaderValue) {

        log.info(":GET:getDraftAccountById: draftAccountId: {}", draftAccountId);

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);
        if (userState.anyBusinessUnitUserHasAnyPermission(Permissions.DRAFT_ACCOUNT_PERMISSIONS)) {
            DraftAccountEntity response = draftAccountService.getDraftAccount(draftAccountId);
            Short buId = response.getBusinessUnit().getBusinessUnitId();
            if (userState.hasBusinessUnitUserWithAnyPermission(buId, Permissions.DRAFT_ACCOUNT_PERMISSIONS)) {
                return buildResponse(Optional.ofNullable(response).map(this::toGetResponseDto).orElse(null));
            } else {
                throw new PermissionNotAllowedException(buId, Permissions.DRAFT_ACCOUNT_PERMISSIONS);
            }
        } else {
            throw new PermissionNotAllowedException(Permissions.DRAFT_ACCOUNT_PERMISSIONS);
        }
    }

    @GetMapping()
    @CheckAcceptHeader
    @Operation(summary = "Returns a collection of draft accounts summaries for the given user.")
    public ResponseEntity<DraftAccountsResponseDto> getDraftAccountSummaries(
        @RequestParam(value = "business_unit") Optional<List<Short>> optionalBusinessUnitIds,
        @RequestParam(value = "status") Optional<List<DraftAccountStatus>> optionalStatus,
        @RequestParam(value = "submitted_by") Optional<List<String>> optionalSubmittedBys,
        @RequestParam(value = "not_submitted_by") Optional<List<String>> optionalNotSubmittedBys,
        @RequestHeader(value = "Authorization", required = false)  String authHeaderValue) {

        log.info(":GET:getDraftAccountSummaries:");
        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);
        if (userState.anyBusinessUnitUserHasAnyPermission(Permissions.DRAFT_ACCOUNT_PERMISSIONS)) {

            List<String> submittedBys = optionalSubmittedBys.orElse(Collections.emptyList());
            List<String> notSubmitted = optionalNotSubmittedBys.orElse(Collections.emptyList());
            log.info(":GET:getDraftAccountSummaries: submitted by: {}; not submitted: {}", submittedBys, notSubmitted);
            if (!submittedBys.isEmpty() && !notSubmitted.isEmpty()) {
                // Request cannot include both submitted_by and not_submitted_by parameters
                throw new IllegalArgumentException(
                    "Cannot include both 'submitted_by' and 'not_submitted_by' parameters.");
            }

            List<DraftAccountStatus> statuses = optionalStatus.orElse(Collections.emptyList());
            Set<Short> filteredBusinessUnitIds = userState
                .filterBusinessUnitsByBusinessUnitUsersWithAnyPermissions(
                    optionalBusinessUnitIds,
                    Permissions.DRAFT_ACCOUNT_PERMISSIONS
                );
            log.info(":GET:getDraftAccountSummaries: status: {}; filtered business units: {}", statuses,
                     filteredBusinessUnitIds);

            List<DraftAccountEntity> response = draftAccountService
                .getDraftAccounts(filteredBusinessUnitIds, statuses, submittedBys, notSubmitted);

            log.info(":GET:getDraftAccountSummaries: summaries count: {}", response.size());

            // TODO filter responses by permissions of logged in user

            return buildResponse(
                DraftAccountsResponseDto.builder()
                    .summaries(response.stream().map(this::toSummaryDto).toList()).build());
        } else {
            throw new PermissionNotAllowedException(Permissions.DRAFT_ACCOUNT_PERMISSIONS);
        }
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Draft Accounts based upon criteria in request body")
    public ResponseEntity<List<DraftAccountResponseDto>> postDraftAccountsSearch(
        @RequestBody DraftAccountSearchDto criteria,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {

        log.info(":POST:postDraftAccountsSearch: query: \n{}", criteria);

        userStateService.checkForAuthorisedUser(authHeaderValue);

        List<DraftAccountEntity> response = draftAccountService.searchDraftAccounts(criteria);

        return buildResponse(response.stream().map(this::toGetResponseDto).toList());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Creates a Draft Account Entity in the DB based upon data in request body")
    @CheckAcceptHeader
    public ResponseEntity<DraftAccountResponseDto> postDraftAccount(@RequestBody AddDraftAccountRequestDto dto,
                @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {
        log.info(":POST:postDraftAccount: creating a new draft account entity: \n{}", dto.toPrettyJson());

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.hasBusinessUnitUserWithPermission(dto.getBusinessUnitId(),
                                                        Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS)) {
            jsonSchemaValidationService.validateOrError(dto.toJson(), ADD_DRAFT_ACCOUNT_REQUEST_JSON);

            DraftAccountEntity response = draftAccountService.submitDraftAccount(dto);
            return buildCreatedResponse(toGetResponseDto(response));
        } else {
            throw new PermissionNotAllowedException(Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);
        }
    }

    @Hidden
    @DeleteMapping(value = "/{draftAccountId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Deletes the Draft Account for the given draftAccountId.")
    @ConditionalOnProperty(prefix = "opal.testing-support-endpoints", name = "enabled", havingValue = "true")
    public ResponseEntity<String> deleteDraftAccountById(
        @PathVariable Long draftAccountId,
        @RequestHeader(value = "Authorization", required = false)  String authHeaderValue,
        @RequestParam("ignore_missing") Optional<Boolean> ignoreMissing) {

        log.info(":DELETE:deleteDraftAccountById: draftAccountId: {}", draftAccountId);

        userStateService.checkForAuthorisedUser(authHeaderValue);

        draftAccountService.deleteDraftAccount(draftAccountId, ignoreMissing);

        return buildResponse(String.format(ACCOUNT_DELETED_MESSAGE_FORMAT, draftAccountId));
    }

    @PutMapping(value = "/{draftAccountId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Replaces an existing Draft Account Entity in the DB with data in request body")
    @CheckAcceptHeader
    public ResponseEntity<DraftAccountResponseDto> replaceDraftAccount(
        @PathVariable Long draftAccountId,
        @RequestBody ReplaceDraftAccountRequestDto dto,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);
        jsonSchemaValidationService.validateOrError(dto.toJson(), REPLACE_DRAFT_ACCOUNT_REQUEST_JSON);

        if (userState.hasBusinessUnitUserWithPermission(dto.getBusinessUnitId(),
                                                       Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS)) {
            log.info(":PUT:replaceDraftAccount: replacing draft account entity with ID: {} and data: {}",
                     draftAccountId, dto);

            DraftAccountEntity replacedEntity = draftAccountService.replaceDraftAccount(draftAccountId, dto);

            return buildResponse(toGetResponseDto(replacedEntity));
        } else {
            throw new PermissionNotAllowedException(Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);
        }
    }

    @PatchMapping(value = "/{draftAccountId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updates an existing Draft Account Entity in the DB with data in request body")
    @CheckAcceptHeader
    public ResponseEntity<DraftAccountResponseDto> updateDraftAccount(
        @PathVariable Long draftAccountId,
        @RequestBody UpdateDraftAccountRequestDto dto,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);
        jsonSchemaValidationService.validateOrError(dto.toJson(), UPDATE_DRAFT_ACCOUNT_REQUEST_JSON);

        if (userState.hasBusinessUnitUserWithPermission(dto.getBusinessUnitId(),
                                                        Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS)) {

            log.info(":PATCH:updateDraftAccount: replacing draft account entity with ID: {} and data: {}",
                     draftAccountId, dto
            );


            DraftAccountEntity updatedEntity = draftAccountService.updateDraftAccount(draftAccountId, dto);

            return buildResponse(toGetResponseDto(updatedEntity));
        } else {
            throw new PermissionNotAllowedException(Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);
        }
    }


    DraftAccountResponseDto toGetResponseDto(DraftAccountEntity entity) {
        return DraftAccountResponseDto.builder()
            .draftAccountId(entity.getDraftAccountId())
            .businessUnitId(Optional.ofNullable(entity.getBusinessUnit())
                                .map(BusinessUnitEntity::getBusinessUnitId).orElse(null))
            .createdDate(toOffsetDateTime(entity.getCreatedDate()))
            .submittedBy(entity.getSubmittedBy())
            .validatedDate(toOffsetDateTime(entity.getValidatedDate()))
            .validatedBy(entity.getValidatedBy())
            .account(entity.getAccount())
            .accountSnapshot(entity.getAccountSnapshot())
            .accountType(entity.getAccountType())
            .accountStatus(entity.getAccountStatus())
            .timelineData(entity.getTimelineData())
            .accountNumber(entity.getAccountNumber())
            .accountId(entity.getAccountId())
            .build();
    }

    DraftAccountSummaryDto toSummaryDto(DraftAccountEntity entity) {
        return DraftAccountSummaryDto.builder()
            .draftAccountId(entity.getDraftAccountId())
            .businessUnitId(entity.getBusinessUnit().getBusinessUnitId())
            .createdDate(toOffsetDateTime(entity.getCreatedDate()))
            .submittedBy(entity.getSubmittedBy())
            .validatedDate(toOffsetDateTime(entity.getValidatedDate()))
            .validatedBy(entity.getValidatedBy())
            .accountSnapshot(entity.getAccountSnapshot())
            .accountType(entity.getAccountType())
            .accountStatus(entity.getAccountStatus())
            .accountNumber(entity.getAccountNumber())
            .accountId(entity.getAccountId())
            .build();
    }

}
