package uk.gov.hmcts.opal.service.opal;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
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
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.DraftAccountStatus;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.service.opal.jpa.DraftAccountTransactions;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static uk.gov.hmcts.opal.util.DateTimeUtils.toUtcDateTime;
import static uk.gov.hmcts.opal.util.VersionUtils.verifyUpdated;

@Service
@Slf4j(topic = "opal.DraftAccountService")
@RequiredArgsConstructor
@Qualifier("draftAccountService")
public class DraftAccountService {


    public static final String ADD_DRAFT_ACCOUNT_REQUEST_JSON = "addDraftAccountRequest.json";
    public static final String REPLACE_DRAFT_ACCOUNT_REQUEST_JSON = "replaceDraftAccountRequest.json";
    public static final String UPDATE_DRAFT_ACCOUNT_REQUEST_JSON = "updateDraftAccountRequest.json";
    public static final String ACCOUNT_DELETED_MESSAGE_FORMAT = """
        { "message": "Draft Account '%s' deleted"}""";

    private final DraftAccountTransactions draftAccountTransactions;

    private final UserStateService userStateService;

    private final BusinessUnitRepository businessUnitRepository;

    private final JsonSchemaValidationService jsonSchemaValidationService;

    private final DraftAccountMapper draftAccountMapper;

    public DraftAccountResponseDto getDraftAccount(long draftAccountId, String authHeaderValue) {

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasAnyPermission(Permissions.DRAFT_ACCOUNT_PERMISSIONS)) {
            DraftAccountEntity response = draftAccountTransactions.getDraftAccount(draftAccountId);
            Short buId = response.getBusinessUnit().getBusinessUnitId();

            if (userState.hasBusinessUnitUserWithAnyPermission(buId, Permissions.DRAFT_ACCOUNT_PERMISSIONS)) {
                return toGetResponseDto(response);

            } else {
                throw new PermissionNotAllowedException(buId, Permissions.DRAFT_ACCOUNT_PERMISSIONS);

            }
        } else {
            throw new PermissionNotAllowedException(Permissions.DRAFT_ACCOUNT_PERMISSIONS);

        }
    }

    public DraftAccountsResponseDto getDraftAccounts(
        Optional<List<Short>> optionalBusinessUnitIds, Optional<List<DraftAccountStatus>> optionalStatus,
        Optional<List<String>> optionalSubmittedBys, Optional<List<String>> optionalNotSubmittedBys,
        Optional<LocalDate> accountStatusDateFrom, Optional<LocalDate> accountStatusDateTo,
        String authHeaderValue) {

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);
        if (userState.anyBusinessUnitUserHasAnyPermission(Permissions.DRAFT_ACCOUNT_PERMISSIONS)) {

            List<String> submittedBys = optionalSubmittedBys.orElse(Collections.emptyList());
            List<String> notSubmitted = optionalNotSubmittedBys.orElse(Collections.emptyList());
            log.debug(":GET:getDraftAccountSummaries: submitted by: {}; not submitted: {}", submittedBys, notSubmitted);
            if (!submittedBys.isEmpty() && !notSubmitted.isEmpty()) {
                // Request cannot include both submitted_by and not_submitted_by parameters
                throw new IllegalArgumentException(
                    "Cannot include both 'submitted_by' and 'not_submitted_by' parameters.");
            }

            List<DraftAccountStatus> statuses = optionalStatus.orElse(Collections.emptyList());
            log.debug(":GET:getDraftAccountSummaries: status: {}; business ids: {}", statuses, optionalBusinessUnitIds);

            List<DraftAccountEntity> entities = draftAccountTransactions
                .getDraftAccounts(optionalBusinessUnitIds.orElse(Collections.emptyList()),
                                  statuses, submittedBys, notSubmitted, accountStatusDateFrom, accountStatusDateTo);

            log.debug(":GET:getDraftAccountSummaries: pre-auth summaries count: {}", entities.size());

            List<DraftAccountEntity> filtered = entities.stream()
                .filter(e -> userState.hasBusinessUnitUserWithAnyPermission(
                    e.getBusinessUnit().getBusinessUnitId(), Permissions.DRAFT_ACCOUNT_PERMISSIONS))
                .toList();

            log.debug(":GET:getDraftAccountSummaries: filtered summaries count: {}", filtered.size());

            return
                DraftAccountsResponseDto.builder()
                    .summaries(
                        filtered.stream().map(this::toSummaryDto).toList()
                    ).build();
        } else {
            throw new PermissionNotAllowedException(Permissions.DRAFT_ACCOUNT_PERMISSIONS);
        }
    }

    public String deleteDraftAccount(long draftAccountId, boolean checkExists, String authHeaderValue) {
        userStateService.checkForAuthorisedUser(authHeaderValue);

        try {
            boolean deleted =  draftAccountTransactions.deleteDraftAccount(draftAccountId, checkExists,
                                                                           draftAccountTransactions);
            if (deleted) {
                log.debug(":DELETE:deleteDraftAccountById: Deleted Draft Account: {}", draftAccountId);
            }
        } catch (UnexpectedRollbackException | EntityNotFoundException ure) {
            if (checkExists) {
                throw ure;
            }
        }
        return String.format(ACCOUNT_DELETED_MESSAGE_FORMAT, draftAccountId);
    }

    public List<DraftAccountResponseDto> searchDraftAccounts(DraftAccountSearchDto criteria, String authHeaderValue) {
        userStateService.checkForAuthorisedUser(authHeaderValue);

        return draftAccountTransactions.searchDraftAccounts(criteria)
            .stream()
            .map(DraftAccountService::toGetResponseDto)
            .toList();
    }

    public DraftAccountResponseDto submitDraftAccount(AddDraftAccountRequestDto dto, String authHeaderValue) {

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.hasBusinessUnitUserWithPermission(dto.getBusinessUnitId(),
                                                        Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS)) {

            jsonSchemaValidationService.validateOrError(dto.toJson(), ADD_DRAFT_ACCOUNT_REQUEST_JSON);
            return toGetResponseDto(draftAccountTransactions.submitDraftAccount(dto));

        } else {
            throw new PermissionNotAllowedException(Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);
        }

    }

    public DraftAccountResponseDto replaceDraftAccount(Long draftAccountId, ReplaceDraftAccountRequestDto dto,
                                                       String authHeaderValue) {

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);
        jsonSchemaValidationService.validateOrError(dto.toJson(), REPLACE_DRAFT_ACCOUNT_REQUEST_JSON);

        if (userState.hasBusinessUnitUserWithPermission(dto.getBusinessUnitId(),
                                                        Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS)) {
            DraftAccountEntity replacedEntity = draftAccountTransactions.replaceDraftAccount(draftAccountId, dto,
                                                                                             draftAccountTransactions);
            verifyUpdated(replacedEntity, dto, draftAccountId, "replaceDraftAccount");
            log.debug(":PUT:putDraftAccount: replaced with version: {}", replacedEntity.getVersion());

            return toGetResponseDto(replacedEntity);
        } else {
            throw new PermissionNotAllowedException(Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);
        }
    }

    public DraftAccountResponseDto updateDraftAccount(Long draftAccountId, UpdateDraftAccountRequestDto dto,
                                                       String authHeaderValue) {

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);
        jsonSchemaValidationService.validateOrError(dto.toJson(), UPDATE_DRAFT_ACCOUNT_REQUEST_JSON);

        if (userState.hasBusinessUnitUserWithPermission(dto.getBusinessUnitId(),
                                                        Permissions.CHECK_VALIDATE_DRAFT_ACCOUNTS)) {
            DraftAccountEntity updatedEntity = draftAccountTransactions.updateDraftAccount(draftAccountId, dto,
                                                                                           draftAccountTransactions);
            verifyUpdated(updatedEntity, dto, draftAccountId, "updateDraftAccount");

            return toGetResponseDto(updatedEntity);
        } else {
            throw new PermissionNotAllowedException(Permissions.CHECK_VALIDATE_DRAFT_ACCOUNTS);
        }
    }

    public static DraftAccountResponseDto toGetResponseDto(DraftAccountEntity entity) {
        return DraftAccountResponseDto.builder()
            .draftAccountId(entity.getDraftAccountId())
            .businessUnitId(Optional.ofNullable(entity.getBusinessUnit())
                                .map(BusinessUnitEntity::getBusinessUnitId).orElse(null))
            .createdDate(toUtcDateTime(entity.getCreatedDate()))
            .submittedBy(entity.getSubmittedBy())
            .submittedByName(entity.getSubmittedByName())
            .validatedDate(toUtcDateTime(entity.getValidatedDate()))
            .validatedBy(entity.getValidatedBy())
            .validatedByName(entity.getValidatedByName())
            .account(entity.getAccount())
            .accountSnapshot(entity.getAccountSnapshot())
            .accountType(entity.getAccountType())
            .accountStatus(entity.getAccountStatus())
            .accountStatusDate(toUtcDateTime(entity.getAccountStatusDate()))
            .statusMessage(entity.getStatusMessage())
            .timelineData(entity.getTimelineData())
            .accountNumber(entity.getAccountNumber())
            .accountId(entity.getAccountId())
            .version(entity.getVersion())
            .build();
    }

    public DraftAccountSummaryDto toSummaryDto(DraftAccountEntity entity) {
        return draftAccountMapper.toDto(entity);
    }
}
