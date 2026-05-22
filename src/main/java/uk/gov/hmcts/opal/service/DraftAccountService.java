package uk.gov.hmcts.opal.service;

import static uk.gov.hmcts.opal.util.VersionUtils.extractBigInteger;
import static uk.gov.hmcts.opal.util.VersionUtils.verifyUpdated;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.logging.SecurityEventLoggingService;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.DraftAccountResponseDto;
import uk.gov.hmcts.opal.dto.DraftAccountSummaryDto;
import uk.gov.hmcts.opal.dto.DraftAccountsResponseDto;
import uk.gov.hmcts.opal.dto.ReplaceDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.UpdateDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.mapper.DraftAccountMapper;
import uk.gov.hmcts.opal.service.opal.DraftAccountPdplLoggingService;
import uk.gov.hmcts.opal.service.opal.DraftAccountPdplLoggingService.Action;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.opal.jpa.DraftAccountTransactional;
import uk.gov.hmcts.opal.service.proxy.DraftAccountPublishProxy;
import uk.gov.hmcts.opal.util.MapUtils;


@Service
@Slf4j(topic = "opal.DraftAccountService")
@RequiredArgsConstructor
@Qualifier("draftAccountService")
public class DraftAccountService {


    public static final String ADD_DRAFT_ACCOUNT_REQUEST_JSON =  SchemaPaths.ADD_DRAFT_ACCOUNT_REQUEST;
    public static final String REPLACE_DRAFT_ACCOUNT_REQUEST_JSON =  SchemaPaths.REPLACE_DRAFT_ACCOUNT_REQUEST;
    public static final String UPDATE_DRAFT_ACCOUNT_REQUEST_JSON =  SchemaPaths.UPDATE_DRAFT_ACCOUNT_REQUEST;
    public static final String ACCOUNT_DELETED_MESSAGE_FORMAT = """
        { "message": "Draft Account '%s' deleted"}""";
    public static final String EVENT_ACCOUNT_APPROVAL = "Business Function - Approval of Draft Account";

    private final DraftAccountTransactional draftAccountTransactional;

    private final UserStateService userStateService;

    private final JsonSchemaValidationService jsonSchemaValidationService;

    private final DraftAccountMapper draftAccountMapper;

    private final DraftAccountPublishProxy accountPublishProxy;

    private final DraftAccountPdplLoggingService loggingService;
    private final SecurityEventLoggingService securityEventLoggingService;
    private final Clock clock;

    public DraftAccountResponseDto getDraftAccount(long draftAccountId, String authHeaderValue) {

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasAnyPermission(FinesPermission.DRAFT_ACCOUNT_PERMISSIONS)) {
            DraftAccountEntity response = draftAccountTransactional.getDraftAccount(draftAccountId);
            Short buId = response.getBusinessUnit().getBusinessUnitId();

            loggingService.pdplForDraftAccount(response, Action.GET, userState);

            if (userState.hasBusinessUnitUserWithAnyPermission(buId, FinesPermission.DRAFT_ACCOUNT_PERMISSIONS)) {
                return toGetResponseDto(response);

            } else {
                throw new PermissionNotAllowedException(buId, FinesPermission.DRAFT_ACCOUNT_PERMISSIONS);

            }
        } else {
            throw new PermissionNotAllowedException(FinesPermission.DRAFT_ACCOUNT_PERMISSIONS);

        }
    }

    public DraftAccountsResponseDto getDraftAccounts(
        Optional<List<Short>> optionalBusinessUnitIds, Optional<List<DraftAccountStatus>> optionalStatus,
        Optional<List<String>> optionalSubmittedBys, Optional<List<String>> optionalNotSubmittedBys,
        Optional<LocalDate> accountStatusDateFrom, Optional<LocalDate> accountStatusDateTo,
        String authHeaderValue) {

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);
        if (userState.anyBusinessUnitUserHasAnyPermission(FinesPermission.DRAFT_ACCOUNT_PERMISSIONS)) {

            List<String> submittedBys = optionalSubmittedBys.orElse(Collections.emptyList());
            List<String> notSubmitted = optionalNotSubmittedBys.orElse(Collections.emptyList());
            log.debug(":getDraftAccounts: submitted by: {}; not submitted: {}", submittedBys, notSubmitted);
            if (!submittedBys.isEmpty() && !notSubmitted.isEmpty()) {
                // Request cannot include both submitted_by and not_submitted_by parameters
                throw new IllegalArgumentException(
                    "Cannot include both 'submitted_by' and 'not_submitted_by' parameters.");
            }

            List<DraftAccountStatus> statuses = optionalStatus.orElse(Collections.emptyList());
            log.debug(":getDraftAccounts: status: {}; business ids: {}", statuses, optionalBusinessUnitIds);

            List<DraftAccountEntity> entities = draftAccountTransactional
                .getDraftAccounts(optionalBusinessUnitIds.orElse(Collections.emptyList()),
                                  statuses, submittedBys, notSubmitted, accountStatusDateFrom, accountStatusDateTo);

            log.debug(":getDraftAccounts: pre-auth summaries count: {}", entities.size());

            List<DraftAccountEntity> filtered = entities.stream()
                .filter(e -> userState.hasBusinessUnitUserWithAnyPermission(
                    e.getBusinessUnit().getBusinessUnitId(), FinesPermission.DRAFT_ACCOUNT_PERMISSIONS))
                .toList();

            log.debug(":getDraftAccounts: filtered summaries count: {}", filtered.size());
            filtered.forEach(draft -> log.debug(":getDraftAccounts: {}", draft.toString()));

            loggingService.logForMultipleGets(filtered, Action.GET, userState);

            return
                DraftAccountsResponseDto.builder()
                    .summaries(
                        filtered.stream().map(this::toSummaryDto).toList()
                    ).build();
        } else {
            throw new PermissionNotAllowedException(FinesPermission.DRAFT_ACCOUNT_PERMISSIONS);
        }
    }

    public String deleteDraftAccount(long draftAccountId, boolean checkExisted, String authHeaderValue) {
        userStateService.checkForAuthorisedUser(authHeaderValue);

        try {
            boolean deleted =  draftAccountTransactional.deleteDraftAccount(draftAccountId, draftAccountTransactional);
            if (deleted) {
                log.debug(":deleteDraftAccount: Deleted Draft Account: {}", draftAccountId);
            }
        } catch (UnexpectedRollbackException | EntityNotFoundException ure) {
            if (checkExisted) {
                throw ure;
            }
        }
        return String.format(ACCOUNT_DELETED_MESSAGE_FORMAT, draftAccountId);
    }

    public List<DraftAccountResponseDto> searchDraftAccounts(DraftAccountSearchDto criteria, String authHeaderValue) {
        userStateService.checkForAuthorisedUser(authHeaderValue);

        return draftAccountTransactional.searchDraftAccounts(criteria)
            .stream()
            .map(draftAccountMapper::toResponseDto)
            .toList();
    }

    public DraftAccountResponseDto submitDraftAccount(AddDraftAccountRequestDto dto, String authHeaderValue) {

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.hasBusinessUnitUserWithPermission(dto.getBusinessUnitId(),
                                                        FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS)) {

            BusinessUnitUser unitUser = getBusinessUnitUserOrThrow(userState, dto.getBusinessUnitId());
            applySubmittedBy(dto, userState, unitUser);

            jsonSchemaValidationService.validateOrError(dto.toJson(), ADD_DRAFT_ACCOUNT_REQUEST_JSON);
            DraftAccountEntity entity = draftAccountTransactional.submitDraftAccount(dto);
            log.debug(":submitDraftAccount: created in DB: {}", entity);

            loggingService.pdplForDraftAccount(entity, Action.SUBMIT, userState);

            return draftAccountMapper.toResponseDto(entity);

        } else {
            throw new PermissionNotAllowedException(
                dto.getBusinessUnitId(),
                FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS);
        }

    }

    public DraftAccountResponseDto replaceDraftAccount(Long draftAccountId, ReplaceDraftAccountRequestDto dto,
                                                       String authHeaderValue, String ifMatch) {

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.hasBusinessUnitUserWithPermission(dto.getBusinessUnitId(),
                                                        FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS)) {
            BusinessUnitUser unitUser = getBusinessUnitUserOrThrow(userState, dto.getBusinessUnitId());
            applySubmittedBy(dto, userState, unitUser);
            jsonSchemaValidationService.validateOrError(dto.toJson(), REPLACE_DRAFT_ACCOUNT_REQUEST_JSON);

            DraftAccountEntity replacedEntity = draftAccountTransactional
                .replaceDraftAccount(draftAccountId, dto, draftAccountTransactional, ifMatch);
            verifyUpdated(replacedEntity, dto, draftAccountId, "replaceDraftAccount");
            log.debug(":replaceDraftAccount: replaced with version: {}", replacedEntity.getVersion());

            loggingService.pdplForDraftAccount(replacedEntity, Action.REPLACE, userState);


            return draftAccountMapper.toResponseDto(replacedEntity);
        } else {
            throw new PermissionNotAllowedException(
                dto.getBusinessUnitId(),
                FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS);
        }
    }

    public DraftAccountResponseDto updateDraftAccount(Long draftAccountId, UpdateDraftAccountRequestDto dto,
        String authHeaderValue, String ifMatch) {

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);
        Optional<BusinessUnitUser> unitUser = userState.getBusinessUnitUserForBusinessUnit(dto.getBusinessUnitId());
        log.info(":updateDraftAccount: unit user: {}", unitUser);
        if (UserState.userHasPermission(unitUser, FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS)) {
            if (DraftAccountStatus.PUBLISHING_PENDING.equals(dto.getAccountStatus())) {
                applyValidatedBy(dto, userState, unitUser.orElseThrow());
            }
            jsonSchemaValidationService.validateOrError(dto.toJson(), UPDATE_DRAFT_ACCOUNT_REQUEST_JSON);

            BigInteger updateVersion = extractBigInteger(ifMatch);

            DraftAccountEntity updatedEntity = draftAccountTransactional.updateDraftAccount(draftAccountId, dto,
                draftAccountTransactional, updateVersion, userState);
            verifyUpdated(updatedEntity, updateVersion, draftAccountId, "updateDraftAccount");

            loggingService.pdplForDraftAccount(updatedEntity, Action.RESUBMIT, userState);

            if (updatedEntity.getAccountStatus().isPublishingPending()) {
                log.info(":updateDraftAccount: publishing: ");
                DraftAccountEntity entity = accountPublishProxy.publishDefendantAccount(
                    updatedEntity, unitUser.orElseThrow());
                logApprovalSuccess(dto.getBusinessUnitId(), userState.getUserId(), draftAccountId,
                    updatedEntity.getSubmittedBy());
                return draftAccountMapper.toResponseDto(entity);
            }

            return draftAccountMapper.toResponseDto(updatedEntity);
        }

        throw new PermissionNotAllowedException(FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS);
    }

    private void logApprovalSuccess(Short buId, Long approverId, Long accountId, String submittedBy) {
        Map<String, Object> data = MapUtils.ofNullable("UserIdentifier", approverId,
            "DraftAccountIdentifier", accountId,
            "DraftAccountSubmittedByUserIdentifier", submittedBy);;
        securityEventLoggingService.logEvent(EVENT_ACCOUNT_APPROVAL, "Success", buId, "Approval",
            LocalDateTime.now(clock), data);
    }

    public DraftAccountResponseDto toGetResponseDto(DraftAccountEntity entity) {
        return draftAccountMapper.toResponseDto(entity);
    }

    public DraftAccountSummaryDto toSummaryDto(DraftAccountEntity entity) {
        return draftAccountMapper.toDto(entity);
    }

    private BusinessUnitUser getBusinessUnitUserOrThrow(UserState userState, Short businessUnitId) {
        return userState.getBusinessUnitUserForBusinessUnit(businessUnitId)
            .orElseThrow(() -> new PermissionNotAllowedException(businessUnitId,
                                                                FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS));
    }

    private void applySubmittedBy(AddDraftAccountRequestDto dto, UserState userState, BusinessUnitUser unitUser) {
        dto.setSubmittedBy(unitUser.getBusinessUnitUserId());
        dto.setSubmittedByName(userState.getUserName());
        dto.setValidatedBy(null);
    }

    private void applySubmittedBy(ReplaceDraftAccountRequestDto dto, UserState userState, BusinessUnitUser unitUser) {
        dto.setSubmittedBy(unitUser.getBusinessUnitUserId());
        dto.setSubmittedByName(userState.getUserName());
    }

    private void applyValidatedBy(UpdateDraftAccountRequestDto dto, UserState userState, BusinessUnitUser unitUser) {
        dto.setValidatedBy(unitUser.getBusinessUnitUserId());
        dto.setValidatedByName(userState.getUserName());
    }
}
