package uk.gov.hmcts.opal.service.opal;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.authorisation.aspect.PermissionNotAllowedException;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.DraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.DraftAccountResponseDto;
import uk.gov.hmcts.opal.dto.DraftAccountSummaryDto;
import uk.gov.hmcts.opal.dto.DraftAccountsResponseDto;
import uk.gov.hmcts.opal.dto.ReplaceDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.UpdateDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.DraftAccountSnapshots;
import uk.gov.hmcts.opal.entity.DraftAccountStatus;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.DraftAccountSpecs;
import uk.gov.hmcts.opal.service.opal.proxy.DraftAccountServiceProxy;
import uk.gov.hmcts.opal.util.JsonPathUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.opal.util.DateTimeUtils.toUtcDateTime;
import static uk.gov.hmcts.opal.util.JsonPathUtil.createDocContext;
import static uk.gov.hmcts.opal.util.VersionUtils.verifyUpdated;
import static uk.gov.hmcts.opal.util.VersionUtils.verifyVersions;

@Service
@Slf4j(topic = "opal.DraftAccountService")
@RequiredArgsConstructor
@Qualifier("draftAccountService")
public class DraftAccountService implements DraftAccountServiceProxy {

    private static final String DEFENDANT_JSON_PATH = "$.defendant";

    private static final EnumSet<DraftAccountStatus> VALID_UPDATE_STATUSES =
        EnumSet.of(DraftAccountStatus.PENDING, DraftAccountStatus.REJECTED, DraftAccountStatus.DELETED);

    private final DraftAccountRepository draftAccountRepository;

    private final BusinessUnitRepository businessUnitRepository;

    private final UserStateService userStateService;

    private final JsonSchemaValidationService jsonSchemaValidationService;

    private final DraftAccountSpecs specs = new DraftAccountSpecs();

    public static final String ADD_DRAFT_ACCOUNT_REQUEST_JSON = "addDraftAccountRequest.json";
    public static final String REPLACE_DRAFT_ACCOUNT_REQUEST_JSON = "replaceDraftAccountRequest.json";
    public static final String UPDATE_DRAFT_ACCOUNT_REQUEST_JSON = "updateDraftAccountRequest.json";
    public static final String ACCOUNT_DELETED_MESSAGE_FORMAT = """
        { "message": "Draft Account '%s' deleted"}""";

    @Transactional(readOnly = true)
    public DraftAccountResponseDto getDraftAccount(long draftAccountId, String authHeaderValue) {

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasAnyPermission(Permissions.DRAFT_ACCOUNT_PERMISSIONS)) {
            DraftAccountEntity response = getDraftAccountEntity(draftAccountId);
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

    @Transactional
    private DraftAccountEntity getDraftAccountEntity(long draftAccountId) {
        return draftAccountRepository.findById(draftAccountId)
            .orElseThrow(() -> new EntityNotFoundException("Draft Account not found with id: " + draftAccountId));
    }

    @Transactional(readOnly = true)
    public DraftAccountsResponseDto getDraftAccounts(Optional<List<Short>> optionalBusinessUnitIds,
                                                     Optional<List<DraftAccountStatus>> optionalStatus,
                                                     Optional<List<String>> optionalSubmittedBys,
                                                     Optional<List<String>> optionalNotSubmittedBys,
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

            Page<DraftAccountEntity> page = draftAccountRepository
                .findBy(specs.findForSummaries(optionalBusinessUnitIds.orElse(Collections.emptyList()),
                                               statuses, submittedBys, notSubmitted),
                        ffq -> ffq.page(Pageable.unpaged()));

            log.debug(":GET:getDraftAccountSummaries: pre-auth summaries count: {}", page.getContent().size());

            List<DraftAccountEntity> filtered = page.getContent().stream()
                .filter(e -> userState.hasBusinessUnitUserWithAnyPermission(
                    e.getBusinessUnit().getBusinessUnitId(), Permissions.DRAFT_ACCOUNT_PERMISSIONS))
                .toList();

            log.debug(":GET:getDraftAccountSummaries: filtered summaries count: {}", filtered.size());

            return
                DraftAccountsResponseDto.builder()
                    .summaries(
                        filtered.stream()
                            .map(this::toSummaryDto)
                            .toList()
                    ).build();
        } else {
            throw new PermissionNotAllowedException(Permissions.DRAFT_ACCOUNT_PERMISSIONS);
        }
    }

    @Transactional
    public String deleteDraftAccount(long draftAccountId, boolean checkExists,
                                      String authHeaderValue) {


        userStateService.checkForAuthorisedUser(authHeaderValue);

        try {
            draftAccountRepository.delete(draftAccountRepository.findById(draftAccountId)
                                              .orElseThrow(() -> new
                                                  EntityNotFoundException("Draft Account not found with id: "
                                                                              + draftAccountId)));

        } catch (UnexpectedRollbackException | EntityNotFoundException ure) {
            if (checkExists) {
                throw ure;
            }
        }
        log.debug(":DELETE:deleteDraftAccountById: Deleted Draft Account: {}", draftAccountId);
        return String.format(ACCOUNT_DELETED_MESSAGE_FORMAT, draftAccountId);
    }

    @Transactional(readOnly = true)
    public List<DraftAccountResponseDto> searchDraftAccounts(DraftAccountSearchDto criteria, String authHeaderValue) {
        userStateService.checkForAuthorisedUser(authHeaderValue);

        Page<DraftAccountEntity> page = draftAccountRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent().stream().map(this::toGetResponseDto).toList();
    }

    @Transactional
    public DraftAccountResponseDto submitDraftAccount(AddDraftAccountRequestDto dto, String authHeaderValue) {

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.hasBusinessUnitUserWithPermission(dto.getBusinessUnitId(),
                                                        Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS)) {

            jsonSchemaValidationService.validateOrError(dto.toJson(), ADD_DRAFT_ACCOUNT_REQUEST_JSON);

            LocalDateTime created = LocalDateTime.now();
            BusinessUnitEntity businessUnit = businessUnitRepository.getReferenceById(dto.getBusinessUnitId());
            String snapshot = createInitialSnapshot(dto, created, businessUnit);

            log.debug(":submitDraftAccount: dto: \n{}", dto.toPrettyJson());
            return toGetResponseDto(draftAccountRepository.save(toEntity(dto, created, businessUnit, snapshot)));

        } else {
            throw new PermissionNotAllowedException(Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);
        }

    }

    @Transactional
    public DraftAccountResponseDto replaceDraftAccount(Long draftAccountId, ReplaceDraftAccountRequestDto dto,
                                                       String authHeaderValue) {

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);
        jsonSchemaValidationService.validateOrError(dto.toJson(), REPLACE_DRAFT_ACCOUNT_REQUEST_JSON);

        if (userState.hasBusinessUnitUserWithPermission(dto.getBusinessUnitId(),
                                                        Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS)) {

            DraftAccountEntity existingAccount = getDraftAccountEntity(draftAccountId);
            verifyVersions(existingAccount, dto, draftAccountId, "replaceDraftAccount");

            BusinessUnitEntity businessUnit = businessUnitRepository.findById(dto.getBusinessUnitId())
                .orElseThrow(() -> new RuntimeException("Business Unit not found with id: " + dto.getBusinessUnitId()));

            if (!(existingAccount.getBusinessUnit().getBusinessUnitId().equals(dto.getBusinessUnitId()))) {
                log.debug("DTO BU does not match entity for draft account with ID: {}", draftAccountId);
                throw new ResourceConflictException(
                    "DraftAccount", Long.toString(draftAccountId),
                    "Business Unit ID mismatch. Existing: "
                        + existingAccount.getBusinessUnit().getBusinessUnitId()
                        + ", Requested: "
                        + dto.getBusinessUnitId()
                );
            }

            String newSnapshot = createUpdateSnapshot(dto, existingAccount.getCreatedDate(), businessUnit);
            existingAccount.setSubmittedBy(dto.getSubmittedBy());
            existingAccount.setSubmittedByName(dto.getSubmittedByName());
            existingAccount.setAccount(dto.getAccount());
            existingAccount.setAccountSnapshot(newSnapshot);
            existingAccount.setAccountType(dto.getAccountType());
            existingAccount.setAccountStatus(DraftAccountStatus.RESUBMITTED);
            existingAccount.setAccountStatusDate(LocalDateTime.now());
            existingAccount.setTimelineData(dto.getTimelineData());

            log.debug(":replaceDraftAccount: Replacing draft account with ID: {} and new snapshot: \n{}",
                      draftAccountId, newSnapshot);
            DraftAccountEntity replacedEntity = draftAccountRepository.save(existingAccount);
            verifyUpdated(replacedEntity, dto, draftAccountId, "putDraftAccount");
            log.debug(":PUT:putDraftAccount: replaced with version: {}", replacedEntity.getVersion());

            return toGetResponseDto(replacedEntity);

        } else {
            throw new PermissionNotAllowedException(Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);
        }
    }

    @Transactional
    public DraftAccountResponseDto updateDraftAccount(Long draftAccountId, UpdateDraftAccountRequestDto dto,
                                                       String authHeaderValue) {

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);
        jsonSchemaValidationService.validateOrError(dto.toJson(), UPDATE_DRAFT_ACCOUNT_REQUEST_JSON);

        if (userState.hasBusinessUnitUserWithPermission(dto.getBusinessUnitId(),
                                                        Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS)) {

            DraftAccountEntity existingAccount = getDraftAccountEntity(draftAccountId);
            verifyVersions(existingAccount, dto, draftAccountId, "updateDraftAccount");

            if (!(existingAccount.getBusinessUnit().getBusinessUnitId().equals(dto.getBusinessUnitId()))) {
                log.warn("DTO BU does not match entity for draft account with ID: {}", draftAccountId);
                throw new ResourceConflictException(
                    "DraftAccount", Long.toString(draftAccountId),
                    "Business Unit ID mismatch. Existing: "
                        + existingAccount.getBusinessUnit().getBusinessUnitId()
                        + ", Requested: "
                        + dto.getBusinessUnitId()
                );
            }

            DraftAccountStatus newStatus = Optional.ofNullable(dto.getAccountStatus())
                .map(String::toUpperCase)
                .map(DraftAccountStatus::valueOf)
                .filter(VALID_UPDATE_STATUSES::contains)
                .orElseThrow(() -> new IllegalArgumentException("Invalid account status for update: "
                                                                    + dto.getAccountStatus()));

            existingAccount.setAccountStatus(newStatus);
            existingAccount.setVersion(dto.getVersion());

            if (newStatus == DraftAccountStatus.PENDING) {
                existingAccount.setValidatedDate(LocalDateTime.now());
                existingAccount.setValidatedBy(dto.getValidatedBy());
                existingAccount.setValidatedByName(dto.getValidatedByName());
                existingAccount.setAccountSnapshot(addSnapshotApprovedDate(existingAccount));
                existingAccount.setAccountStatusDate(LocalDateTime.now());
            }
            // Set the timeline data as received from the front end
            existingAccount.setTimelineData(dto.getTimelineData());

            log.debug(":updateDraftAccount: Updating draft account with ID: {} and status: {}",
                      draftAccountId, existingAccount.getAccountStatus());

            DraftAccountEntity updatedEntity = draftAccountRepository.save(existingAccount);
            verifyUpdated(updatedEntity, dto, draftAccountId, "patchDraftAccount");

            return toGetResponseDto(updatedEntity);
        } else {
            throw new PermissionNotAllowedException(Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);
        }
    }

    private String addSnapshotApprovedDate(DraftAccountEntity existingAccount) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = (ObjectNode) mapper.readTree(existingAccount.getAccountSnapshot());

            String approvedDate = toUtcDateTime(existingAccount.getValidatedDate())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            rootNode.put("approved_date", approvedDate);

            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON in addSnapshotApprovedDate", e);
        }
    }

    private String createInitialSnapshot(AddDraftAccountRequestDto dto, LocalDateTime created,
                                         BusinessUnitEntity businessUnit) {
        return buildSnapshot(dto.getAccount(), created, businessUnit, dto.getSubmittedBy(), dto.getSubmittedByName(),
                             "AddDraftAccountRequestDto.account")
            .toPrettyJson();
    }

    private String createUpdateSnapshot(ReplaceDraftAccountRequestDto dto, LocalDateTime created,
                                         BusinessUnitEntity businessUnit) {
        return buildSnapshot(dto.getAccount(), created, businessUnit, dto.getSubmittedBy(), dto.getSubmittedByName(),
                             "ReplaceDraftAccountRequestDto.account")
            .toPrettyJson();
    }

    private  DraftAccountSnapshots.Snapshot buildSnapshot(String document, LocalDateTime created,
                                                          BusinessUnitEntity businessUnit, String submittedBy,
                                                          String submittedByName, String errorSource) {

        JsonPathUtil.DocContext docContext = createDocContext(document, errorSource);

        String companyName = docContext.readOrNull(DEFENDANT_JSON_PATH + ".company_name");

        final boolean notCompany = companyName == null || companyName.isBlank();

        String defendantName = notCompany
            ? docContext.read(DEFENDANT_JSON_PATH + ".surname") + ", "
            + docContext.read(DEFENDANT_JSON_PATH + ".forenames")
            : companyName;

        String dob = notCompany
            ? docContext.read(DEFENDANT_JSON_PATH + ".dob")
            : null;
        String accType = docContext.read("$.account_type");

        return DraftAccountSnapshots.Snapshot.builder()
            .defendantName(defendantName)
            .dateOfBirth(dob)
            .createdDate(toUtcDateTime(created))
            .accountType(accType)
            .submittedBy(submittedBy)
            .submittedByName(submittedByName)
            .businessUnitName(businessUnit.getBusinessUnitName())
            .build();
    }

    DraftAccountEntity toEntity(DraftAccountRequestDto dto, LocalDateTime created,
                                BusinessUnitEntity businessUnit, String snapshot) {
        return DraftAccountEntity.builder()
            .businessUnit(businessUnit)
            .createdDate(created)
            .submittedBy(dto.getSubmittedBy())
            .submittedByName(dto.getSubmittedByName())
            .account(dto.getAccount())
            .accountSnapshot(snapshot)
            .accountType(dto.getAccountType())
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .accountStatusDate(LocalDateTime.now())
            .timelineData(dto.getTimelineData())
            .draftAccountId(null)
            .build();
    }

    public DraftAccountResponseDto toGetResponseDto(DraftAccountEntity entity) {
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
        return DraftAccountSummaryDto.builder()
            .draftAccountId(entity.getDraftAccountId())
            .businessUnitId(entity.getBusinessUnit().getBusinessUnitId())
            .createdDate(toUtcDateTime(entity.getCreatedDate()))
            .submittedBy(entity.getSubmittedBy())
            .validatedDate(toUtcDateTime(entity.getValidatedDate()))
            .validatedBy(entity.getValidatedBy())
            .validatedByName(entity.getValidatedByName())
            .accountSnapshot(entity.getAccountSnapshot())
            .accountType(entity.getAccountType())
            .accountStatus(entity.getAccountStatus())
            .accountNumber(entity.getAccountNumber())
            .accountId(entity.getAccountId())
            .build();
    }
}
