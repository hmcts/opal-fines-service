package uk.gov.hmcts.opal.service.opal.jpa;


import static uk.gov.hmcts.opal.entity.draft.DraftAccountStatus.PUBLISHED;
import static uk.gov.hmcts.opal.entity.draft.StoredProcedureNames.DEF_ACC_ID;
import static uk.gov.hmcts.opal.entity.draft.StoredProcedureNames.DEF_ACC_NO;
import static uk.gov.hmcts.opal.service.DraftAccountService.EVENT_ACCOUNT_APPROVAL;
import static uk.gov.hmcts.opal.util.DateTimeUtils.toUtcDateTime;
import static uk.gov.hmcts.opal.util.JsonPathUtil.createDocContext;
import static uk.gov.hmcts.opal.util.VersionUtils.verifyIfMatch;
import static uk.gov.hmcts.opal.util.VersionUtils.verifyVersions;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.opal.common.logging.SecurityEventLoggingService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.ReplaceDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.UpdateDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity_;
import uk.gov.hmcts.opal.entity.draft.DraftAccountSnapshots;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.entity.draft.TimelineData;
import uk.gov.hmcts.opal.exception.EntityNotSavedException;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.exception.SubmitterDeniedException;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.DraftAccountSpecs;
import uk.gov.hmcts.opal.util.JsonPathUtil;
import uk.gov.hmcts.opal.util.MapUtils;

@Service
@Slf4j(topic = "opal.DraftAccountService")
@RequiredArgsConstructor
@Qualifier("draftAccountService")
public class DraftAccountTransactional implements DraftAccountTransactionalProxy {

    private static final String DEFENDANT_JSON_PATH = "$.defendant";

    private static final EnumSet<DraftAccountStatus> VALID_UPDATE_STATUSES =
        EnumSet.of(DraftAccountStatus.PUBLISHING_PENDING, DraftAccountStatus.REJECTED, DraftAccountStatus.DELETED);
    public static final String EVENT_NAME_DELETION = "Business Function - Deletion of Draft Account";

    private final DraftAccountRepository draftAccountRepository;

    private final BusinessUnitRepository businessUnitRepository;

    private final SecurityEventLoggingService securityEventLoggingService;

    private final Clock clock;

    private final DraftAccountSpecs specs = new DraftAccountSpecs();

    @Transactional(readOnly = true)
    public DraftAccountEntity getDraftAccount(long draftAccountId) {
        return draftAccountRepository.findById(draftAccountId)
            .orElseThrow(() -> new EntityNotFoundException("Draft Account not found with id: " + draftAccountId));
    }

    @Transactional(readOnly = true)
    public List<DraftAccountEntity> getDraftAccounts(
        Collection<Short> businessUnitIds, Collection<DraftAccountStatus> statuses,
        Collection<String> submittedBy, Collection<String> notSubmitted,
        Optional<LocalDate> accountStatusDateFrom, Optional<LocalDate> accountStatusDateTo) {

        Sort draftIdSort = Sort.by(Sort.Direction.ASC, DraftAccountEntity_.DRAFT_ACCOUNT_ID);

        Page<DraftAccountEntity> page = draftAccountRepository.findBy(
            specs.findForSummaries(
                businessUnitIds, statuses, submittedBy, notSubmitted, accountStatusDateFrom, accountStatusDateTo),
            ffq -> ffq.sortBy(draftIdSort).page(Pageable.unpaged()));

        return page.getContent();
    }

    @Transactional
    public boolean deleteDraftAccount(long draftAccountId, DraftAccountTransactionalProxy proxy) {
        draftAccountRepository.delete(proxy.getDraftAccount(draftAccountId));
        return true;
    }

    @Transactional(readOnly = true)
    public List<DraftAccountEntity> searchDraftAccounts(DraftAccountSearchDto criteria) {

        Page<DraftAccountEntity> page = draftAccountRepository
            .findBy(specs.findBySearchCriteria(criteria), ffq -> ffq.page(Pageable.unpaged()));
        return page.getContent();
    }

    @Transactional
    public DraftAccountEntity submitDraftAccount(AddDraftAccountRequestDto dto) {
        LocalDateTime created = LocalDateTime.now(clock);
        BusinessUnitEntity businessUnit = businessUnitRepository.getReferenceById(
            dto.getBusinessUnitId());
        String snapshot = createInitialSnapshot(dto, created, businessUnit);
        log.debug(":submitDraftAccount: dto: \n{}", dto.toPrettyJson());

        return draftAccountRepository.save(
            toEntity(dto, created, businessUnit, snapshot));

    }

    @Transactional
    public DraftAccountEntity replaceDraftAccount(Long draftAccountId, ReplaceDraftAccountRequestDto dto,
                                                  DraftAccountTransactionalProxy proxy, String ifMatch) {
        DraftAccountEntity existingAccount = proxy.getDraftAccount(draftAccountId);
        verifyIfMatch(existingAccount, ifMatch, draftAccountId, "replaceDraftAccount");

        BusinessUnitEntity businessUnit = businessUnitRepository.findById(dto.getBusinessUnitId())
            .orElseThrow(() -> new RuntimeException("Business Unit not found with id: " + dto.getBusinessUnitId()));

        if (!(existingAccount.getBusinessUnit().getBusinessUnitId().equals(dto.getBusinessUnitId()))) {
            log.debug("DTO BU does not match entity for draft account with ID: {}", draftAccountId);
            throw new ResourceConflictException(
                "DraftAccount", Long.toString(draftAccountId),
                "Business Unit ID mismatch. Existing: " + existingAccount.getBusinessUnit().getBusinessUnitId()
                    + ", Requested: " + dto.getBusinessUnitId(), null
            );
        }

        String newSnapshot = createUpdateSnapshot(dto, existingAccount.getCreatedDate(), businessUnit);
        existingAccount.setSubmittedBy(dto.getSubmittedBy());
        existingAccount.setSubmittedByName(dto.getSubmittedByName());
        existingAccount.setAccount(dto.getAccount());
        existingAccount.setAccountSnapshot(newSnapshot);
        existingAccount.setAccountType(dto.getAccountType());
        existingAccount.setAccountStatus(DraftAccountStatus.RESUBMITTED);
        existingAccount.setAccountStatusDate(LocalDateTime.now(clock));
        existingAccount.setTimelineData(
            appendTimelineEntry(
                existingAccount.getTimelineData(),
                dto.getSubmittedBy(),
                DraftAccountStatus.RESUBMITTED,
                null
            )
        );

        log.debug(":replaceDraftAccount: Replacing draft account with ID: {} and new snapshot: \n{}",
                  draftAccountId, newSnapshot);
        return draftAccountRepository.save(existingAccount);

    }

    @Transactional
    public DraftAccountEntity updateDraftAccount(Long draftAccountId, UpdateDraftAccountRequestDto dto,
                                                 DraftAccountTransactionalProxy proxy, BigInteger updateVersion,
                                                 UserState userState) {
        DraftAccountEntity existingAccount = proxy.getDraftAccount(draftAccountId);
        verifyIfMatch(existingAccount, updateVersion, draftAccountId, "updateDraftAccount");

        log.info(":updateDraftAccount: existing account: {}", existingAccount);

        if (!(existingAccount.getBusinessUnit().getBusinessUnitId().equals(dto.getBusinessUnitId()))) {
            log.warn("DTO BU does not match entity for draft account with ID: {}", draftAccountId);
            throw new ResourceConflictException(
                "DraftAccount", Long.toString(draftAccountId),
                "Business Unit ID mismatch. Existing: " + existingAccount.getBusinessUnit().getBusinessUnitId()
                    + ", Requested: " + dto.getBusinessUnitId(), null
            );
        }

        DraftAccountStatus newStatus = Optional.ofNullable(dto.getAccountStatus())
            .filter(VALID_UPDATE_STATUSES::contains)
            .orElseThrow(() -> new IllegalArgumentException("Invalid account status for update: "
                                                                + dto.getAccountStatus()));

        log.info(":updateDraftAccount: new status: {}", newStatus);
        existingAccount.setAccountStatus(newStatus);
        existingAccount.setVersionNumber(updateVersion.longValueExact());

        if (newStatus.isPublishingPending()) {
            LocalDateTime validationTimestamp = LocalDateTime.now(clock);
            checkValidatorIsNotSubmitter(existingAccount.getSubmittedBy(), dto.getValidatedBy(), draftAccountId,
                userState, dto.getBusinessUnitId());
            existingAccount.setValidatedDate(validationTimestamp);
            existingAccount.setValidatedBy(dto.getValidatedBy());
            existingAccount.setValidatedByName(dto.getValidatedByName());
            existingAccount.setAccountSnapshot(addSnapshotApprovedDate(existingAccount));
            existingAccount.setAccountStatusDate(validationTimestamp);
        }

        if (newStatus.isDeleted()) {
            checkDeleterIsNotSubmitter(existingAccount.getSubmittedBy(), userState.getUserName(), draftAccountId,
                userState, dto.getBusinessUnitId());
        }

        existingAccount.setTimelineData(
            appendTimelineEntry(existingAccount.getTimelineData(), dto.getValidatedBy(), newStatus, dto.getReasonText())
        );

        log.info(":updateDraftAccount: Updating draft account with ID: {} and status: {}",
                  draftAccountId, existingAccount.getAccountStatus());

        return draftAccountRepository.save(existingAccount);

    }

    @Transactional
    public DraftAccountEntity updateStatus(DraftAccountEntity entity, DraftAccountStatus status,
                                           DraftAccountTransactionalProxy proxy) {

        Long draftAccountId = entity.getDraftAccountId();
        log.debug(":updateStatus: Updating draft account with ID: {} to status: {}",
                  draftAccountId, status);
        DraftAccountEntity dbDraftAccount = proxy.getDraftAccount(draftAccountId);
        verifyVersions(dbDraftAccount, entity, draftAccountId, "updateStatus");

        dbDraftAccount.setAccountStatus(status);
        dbDraftAccount.setVersionNumber(entity.getVersion().longValueExact());
        dbDraftAccount.setAccountStatusDate(LocalDateTime.now(clock));

        // These are specific to the results from the 'publish' activity, success
        dbDraftAccount.setAccountNumber(entity.getAccountNumber());
        dbDraftAccount.setAccountId(entity.getAccountId());
        // These are specific to the results from the 'publish' activity, failure
        dbDraftAccount.setStatusMessage(entity.getStatusMessage());
        dbDraftAccount.setTimelineData(entity.getTimelineData());
        return draftAccountRepository.save(dbDraftAccount);
    }

    @Transactional
    public DraftAccountEntity publishDefendantAccount(DraftAccountEntity publishEntity) {
        Map<String, Object> outputs = publishAccountStoredProc(publishEntity);
        String accountNumber = outputs.getOrDefault(DEF_ACC_NO, "<null>").toString();
        Long accountId = Long.parseLong(outputs.getOrDefault(DEF_ACC_ID, "0").toString());
        log.debug(":publishDefendantAccount: \n\nPublished,  account number: {}, account id: {}\n\n",
            accountNumber, accountId);

        Long draftAccountId = publishEntity.getDraftAccountId();
        DraftAccountEntity dbDraftAccount = this.getDraftAccount(draftAccountId);
        verifyVersions(dbDraftAccount, publishEntity, draftAccountId, "updateStatus");

        dbDraftAccount.setAccountStatus(PUBLISHED);
        dbDraftAccount.setVersionNumber(publishEntity.getVersion().longValueExact());
        dbDraftAccount.setAccountStatusDate(LocalDateTime.now(clock));

        // These are specific to the results from the 'publish' activity, success
        dbDraftAccount.setAccountNumber(accountNumber);
        dbDraftAccount.setAccountId(accountId);
        // These are specific to the results from the 'publish' activity, failure
        dbDraftAccount.setStatusMessage(publishEntity.getStatusMessage());
        dbDraftAccount.setTimelineData(publishEntity.getTimelineData());
        try {
            return draftAccountRepository.saveAndFlush(dbDraftAccount);
        } catch (Exception e) {
            throw new EntityNotSavedException("Unable to save draft account", e);
        }
    }

    private Map<String, Object> publishAccountStoredProc(DraftAccountEntity publishEntity) {

        return draftAccountRepository.createDefendantAccount(publishEntity.getDraftAccountId(),
                                                             publishEntity.getBusinessUnit().getBusinessUnitId(),
                                                             publishEntity.getSubmittedBy(),
                                                             publishEntity.getSubmittedByName());
    }

    private String addSnapshotApprovedDate(DraftAccountEntity existingAccount) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = (ObjectNode) mapper.readTree(existingAccount.getAccountSnapshot());

            String approvedDate = toUtcDateTime(existingAccount.getValidatedDate())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            rootNode.put("approved_date", approvedDate);

            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        } catch (JacksonException e) {
            throw new RuntimeException("Error processing JSON in addSnapshotApprovedDate", e);
        }
    }

    private String createInitialSnapshot(AddDraftAccountRequestDto dto, LocalDateTime created,
                                         BusinessUnitEntity businessUnit) {
        return buildSnapshot(dto.getAccount(), created, businessUnit, dto.getSubmittedBy(), dto.getSubmittedByName(),
                             "AddDraftAccountRequestDto.account").toPrettyJson();
    }

    private String createUpdateSnapshot(ReplaceDraftAccountRequestDto dto, LocalDateTime created,
                                         BusinessUnitEntity businessUnit) {
        return buildSnapshot(dto.getAccount(), created, businessUnit, dto.getSubmittedBy(), dto.getSubmittedByName(),
                             "ReplaceDraftAccountRequestDto.account").toPrettyJson();
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

    DraftAccountEntity toEntity(AddDraftAccountRequestDto dto, LocalDateTime created,
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
            .accountStatusDate(created)
            .statusMessage(dto.getStatusMessage())
            .timelineData(createTimelineData(dto.getSubmittedBy(), DraftAccountStatus.SUBMITTED, null))
            .draftAccountId(null)
            .build();
    }

    private String createTimelineData(String username, DraftAccountStatus status, String reasonText) {
        return appendTimelineEntry(null, username, status, reasonText);
    }

    private String appendTimelineEntry(String existingTimelineData, String username, DraftAccountStatus status,
                                       String reasonText) {
        TimelineData timelineData = existingTimelineData == null || existingTimelineData.isBlank()
            ? new TimelineData()
            : new TimelineData(existingTimelineData);
        timelineData.insertEntry(username, status.getLabel(), LocalDate.now(clock), reasonText);
        return timelineData.toJson();
    }

    private void checkValidatorIsNotSubmitter(String submitterUsername, String updaterUserName, Long draftAccountId,
        UserState userState, Short businessUnitId) {
        if (submitterUsername != null && submitterUsername.equals(updaterUserName)) {
            Map<String, Object> data = getSecurityLogDataMap(userState.getUserId(), draftAccountId, submitterUsername);
            securityEventLoggingService.logEvent(EVENT_ACCOUNT_APPROVAL, "Failure", businessUnitId,
                "Approval", LocalDateTime.now(clock), data);
            throw new SubmitterDeniedException(submitterUsername, "validate");
        }
    }

    private void checkDeleterIsNotSubmitter(String submitterUsername, String updaterUserName, Long draftAccountId,
        UserState userState, Short businessUnitId) {
        if (submitterUsername != null && submitterUsername.equals(updaterUserName)) {
            Map<String, Object> data = getSecurityLogDataMap(userState.getUserId(), draftAccountId, submitterUsername);
            securityEventLoggingService.logEvent(EVENT_NAME_DELETION,
                  "Failure", businessUnitId, "Deletion", LocalDateTime.now(clock), data);
            throw new SubmitterDeniedException(submitterUsername, "delete");
        }
    }

    private Map<String, Object> getSecurityLogDataMap(Long approverId, Long accountId, String submittedBy) {
        return MapUtils.ofNullable("UserIdentifier", approverId,
            "DraftAccountIdentifier", accountId,
            "DraftAccountSubmittedByUserIdentifier", submittedBy);
    }

}
