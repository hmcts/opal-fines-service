package uk.gov.hmcts.opal.service.opal.jpa;


import static uk.gov.hmcts.opal.util.DateTimeUtils.toUtcDateTime;
import static uk.gov.hmcts.opal.util.JsonPathUtil.createDocContext;
import static uk.gov.hmcts.opal.util.VersionUtils.verifyIfMatch;
import static uk.gov.hmcts.opal.util.VersionUtils.verifyVersions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
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
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.DraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.ReplaceDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.UpdateDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity_;
import uk.gov.hmcts.opal.entity.draft.DraftAccountSnapshots;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.exception.SubmitterCannotValidateException;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.DraftAccountSpecs;
import uk.gov.hmcts.opal.service.opal.DraftAccountPdplLoggingService;
import uk.gov.hmcts.opal.service.opal.DraftAccountPdplLoggingService.Action;
import uk.gov.hmcts.opal.util.JsonPathUtil;

@Service
@Slf4j(topic = "opal.DraftAccountService")
@RequiredArgsConstructor
@Qualifier("draftAccountService")
public class DraftAccountTransactional implements DraftAccountTransactionalProxy {

    private static final String DEFENDANT_JSON_PATH = "$.defendant";

    private static final EnumSet<DraftAccountStatus> VALID_UPDATE_STATUSES =
        EnumSet.of(DraftAccountStatus.PUBLISHING_PENDING, DraftAccountStatus.REJECTED, DraftAccountStatus.DELETED);

    private final DraftAccountRepository draftAccountRepository;

    private final BusinessUnitRepository businessUnitRepository;

    private final DraftAccountSpecs specs = new DraftAccountSpecs();

    private final DraftAccountPdplLoggingService loggingService;

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
        LocalDateTime created = LocalDateTime.now();
        BusinessUnitFullEntity businessUnit = businessUnitRepository.getReferenceById(
            dto.getBusinessUnitId());
        String snapshot = createInitialSnapshot(dto, created, businessUnit);
        log.debug(":submitDraftAccount: dto: \n{}", dto.toPrettyJson());

        DraftAccountEntity draftAccountEntity = draftAccountRepository.save(
            toEntity(dto, created, businessUnit, snapshot));

        loggingService.pdplForDraftAccount(draftAccountEntity, Action.SUBMIT);

        return draftAccountEntity;

    }

    @Transactional
    public DraftAccountEntity replaceDraftAccount(Long draftAccountId, ReplaceDraftAccountRequestDto dto,
                                                  DraftAccountTransactionalProxy proxy, String ifMatch) {
        DraftAccountEntity existingAccount = proxy.getDraftAccount(draftAccountId);
        verifyIfMatch(existingAccount, ifMatch, draftAccountId, "replaceDraftAccount");

        BusinessUnitFullEntity businessUnit = businessUnitRepository.findById(dto.getBusinessUnitId())
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
        existingAccount.setAccountStatusDate(LocalDateTime.now());
        existingAccount.setTimelineData(dto.getTimelineData());

        log.debug(":replaceDraftAccount: Replacing draft account with ID: {} and new snapshot: \n{}",
                  draftAccountId, newSnapshot);
        return draftAccountRepository.save(existingAccount);
    }

    @Transactional
    public DraftAccountEntity updateDraftAccount(Long draftAccountId, UpdateDraftAccountRequestDto dto,
                                                 DraftAccountTransactionalProxy proxy, BigInteger updateVersion) {
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
            .map(DraftAccountStatus::fromLabel)
            .filter(VALID_UPDATE_STATUSES::contains)
            .orElseThrow(() -> new IllegalArgumentException("Invalid account status for update: "
                                                                + dto.getAccountStatus()));

        log.info(":updateDraftAccount: new status: {}", newStatus);
        existingAccount.setAccountStatus(newStatus);
        existingAccount.setVersionNumber(updateVersion.longValueExact());

        if (newStatus.isPublishingPending()) {
            if (existingAccount.getSubmittedBy() != null
                && existingAccount.getSubmittedBy().equals(dto.getValidatedBy())) {
                throw new SubmitterCannotValidateException(
                    "A single user cannot submit and validate the same Draft Account");
            }
            existingAccount.setValidatedDate(LocalDateTime.now());
            existingAccount.setValidatedBy(dto.getValidatedBy());
            existingAccount.setValidatedByName(dto.getValidatedByName());
            existingAccount.setAccountSnapshot(addSnapshotApprovedDate(existingAccount));
            existingAccount.setAccountStatusDate(LocalDateTime.now());
        }
        // Set the timeline data as received from the front end
        existingAccount.setTimelineData(dto.getTimelineData());

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
        dbDraftAccount.setAccountStatusDate(LocalDateTime.now());

        // These are specific to the results from the 'publish' activity, success
        dbDraftAccount.setAccountNumber(entity.getAccountNumber());
        dbDraftAccount.setAccountId(entity.getAccountId());
        // These are specific to the results from the 'publish' activity, failure
        dbDraftAccount.setStatusMessage(entity.getStatusMessage());
        dbDraftAccount.setTimelineData(entity.getTimelineData());

        return draftAccountRepository.save(dbDraftAccount);
    }

    @Transactional
    public Map<String, Object> publishAccountStoredProc(DraftAccountEntity publishEntity) {

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
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON in addSnapshotApprovedDate", e);
        }
    }

    private String createInitialSnapshot(AddDraftAccountRequestDto dto, LocalDateTime created,
                                         BusinessUnitFullEntity businessUnit) {
        return buildSnapshot(dto.getAccount(), created, businessUnit, dto.getSubmittedBy(), dto.getSubmittedByName(),
                             "AddDraftAccountRequestDto.account").toPrettyJson();
    }

    private String createUpdateSnapshot(ReplaceDraftAccountRequestDto dto, LocalDateTime created,
                                         BusinessUnitFullEntity businessUnit) {
        return buildSnapshot(dto.getAccount(), created, businessUnit, dto.getSubmittedBy(), dto.getSubmittedByName(),
                             "ReplaceDraftAccountRequestDto.account").toPrettyJson();
    }

    private  DraftAccountSnapshots.Snapshot buildSnapshot(String document, LocalDateTime created,
                                                          BusinessUnitFullEntity businessUnit, String submittedBy,
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
                                BusinessUnitFullEntity businessUnit, String snapshot) {
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

}
