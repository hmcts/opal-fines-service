package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.DraftAccountResponseDto;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.DraftAccountStatus;
import uk.gov.hmcts.opal.service.opal.DraftAccountService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.opal.util.DateTimeUtils.toOffsetDateTime;
import static uk.gov.hmcts.opal.util.HttpUtil.buildCreatedResponse;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/api/draft-accounts")
@Slf4j(topic = "DraftAccountController")
@Tag(name = "DraftAccount Controller")
public class DraftAccountController {

    public static final String ADD_DRAFT_ACCOUNT_REQUEST_JSON = "addDraftAccountRequest.json";

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
    @Operation(summary = "Returns the Draft Account for the given draftAccountId.")
    public ResponseEntity<DraftAccountResponseDto> getDraftAccountById(
        @PathVariable Long draftAccountId,
        @RequestHeader(value = "Authorization", required = false)  String authHeaderValue) {

        log.info(":GET:getDraftAccountById: draftAccountId: {}", draftAccountId);

        userStateService.checkForAuthorisedUser(authHeaderValue);

        DraftAccountEntity response = draftAccountService.getDraftAccount(draftAccountId);

        return buildResponse(Optional.ofNullable(response).map(this::toGetResponseDto).orElse(null));
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Draft Accounts based upon criteria in request body")
    public ResponseEntity<List<DraftAccountEntity>> postDraftAccountsSearch(@RequestBody DraftAccountSearchDto criteria,
                @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {
        log.info(":POST:postDraftAccountsSearch: query: \n{}", criteria);

        userStateService.checkForAuthorisedUser(authHeaderValue);

        List<DraftAccountEntity> response = draftAccountService.searchDraftAccounts(criteria);

        return buildResponse(response);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Creates a Draft Account Entity in the DB based upon data in request body")
    public ResponseEntity<DraftAccountResponseDto> postDraftAccount(@RequestBody AddDraftAccountRequestDto dto,
                @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {
        log.info(":POST:postDraftAccount: creating a new draft account entity.");

        UserState user = userStateService.checkForAuthorisedUser(authHeaderValue);

        jsonSchemaValidationService.validateOrError(dto.toJson(), ADD_DRAFT_ACCOUNT_REQUEST_JSON);

        DraftAccountEntity response = draftAccountService.submitDraftAccount(dto, user.getUserName());

        return buildCreatedResponse(toGetResponseDto(response));
    }

    DraftAccountResponseDto toGetResponseDto(DraftAccountEntity entity) {
        return DraftAccountResponseDto.builder()
            .draftAccountId(entity.getDraftAccountId())
            .businessUnitId(entity.getBusinessUnit().getBusinessUnitId())
            .createdDate(toOffsetDateTime(entity.getCreatedDate()))
            .submittedBy(entity.getSubmittedBy())
            .validatedDate(toOffsetDateTime(entity.getValidatedDate()))
            .validatedBy(entity.getValidatedBy())
            .account(entity.getAccount())
            .accountSnapshot(entity.getAccountSnapshot())
            .accountType(entity.getAccountType())
            .accountStatus(Optional.ofNullable(entity.getAccountStatus())
                               .map(DraftAccountStatus::getLabel).orElse(null))
            .timelineData(entity.getTimelineData())
            .accountNumber(entity.getAccountNumber())
            .accountId(entity.getAccountId())
            .build();
    }

}
