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
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.DraftAccountRequestDto;
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
import uk.gov.hmcts.opal.util.JsonPathUtil;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.opal.util.JsonPathUtil.createDocContext;

@Service
@Slf4j(topic = "DraftAccountService")
@RequiredArgsConstructor
@Qualifier("draftAccountService")
public class DraftAccountService {

    private static final String A_C_R_JSON_PATH = "$.account_create_request";
    private static final String DEFENDANT_JSON_PATH = A_C_R_JSON_PATH + ".defendant";
    private static final String ACCOUNT_JSON_PATH = A_C_R_JSON_PATH + ".account";

    private static final EnumSet<DraftAccountStatus> VALID_UPDATE_STATUSES =
        EnumSet.of(DraftAccountStatus.PENDING, DraftAccountStatus.REJECTED, DraftAccountStatus.DELETED);

    private final DraftAccountRepository draftAccountRepository;

    private final BusinessUnitRepository businessUnitRepository;

    private final DraftAccountSpecs specs = new DraftAccountSpecs();

    public DraftAccountEntity getDraftAccount(long draftAccountId) {
        return draftAccountRepository.getReferenceById(draftAccountId);
    }

    public List<DraftAccountEntity> getDraftAccounts(Collection<Short> businessUnitIds,
                                                     Collection<DraftAccountStatus> statuses,
                                                     Collection<String> submittedBy,
                                                     Collection<String> notSubmitted) {
        Page<DraftAccountEntity> page = draftAccountRepository
            .findBy(specs.findForSummaries(businessUnitIds, statuses, submittedBy, notSubmitted),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

    public void deleteDraftAccount(long draftAccountId, Optional<Boolean> ignoreMissing) {
        DraftAccountEntity entity = getDraftAccount(draftAccountId);
        // If the DB doesn't hold the target entity to be deleted, then no exception is thrown when a deletion is
        // attempted. So we need to retrieve the entity first and try to access any property.
        // This will throw an exception if the entity doesn't exist.
        boolean checkExists = !(ignoreMissing.orElse(false));
        if (checkExists && entity.getCreatedDate() == null) {
            // Will not get here, as JPA should throw an exception. But for testing, throw an Exception.
            throw new RuntimeException("Draft Account entity '" + draftAccountId + "' does not exist in the DB.");
        } else {
            draftAccountRepository.delete(entity);
        }
    }

    public List<DraftAccountEntity> searchDraftAccounts(DraftAccountSearchDto criteria) {
        Page<DraftAccountEntity> page = draftAccountRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

    public DraftAccountEntity submitDraftAccount(AddDraftAccountRequestDto dto) {
        LocalDateTime created = LocalDateTime.now();
        BusinessUnitEntity businessUnit = businessUnitRepository.findById(dto.getBusinessUnitId()).orElse(null);
        String snapshot = createInitialSnapshot(dto, created, businessUnit);
        log.info(":submitDraftAccount: dto: \n{}", dto.toPrettyJson());
        return draftAccountRepository.save(toEntity(dto, created, businessUnit, snapshot));
    }

    public DraftAccountEntity replaceDraftAccount(Long draftAccountId, ReplaceDraftAccountRequestDto dto) {
        DraftAccountEntity existingAccount = draftAccountRepository.findById(draftAccountId)
            .orElseThrow(() -> new EntityNotFoundException("Draft Account not found with id: " + draftAccountId));

        BusinessUnitEntity businessUnit = businessUnitRepository.findById(dto.getBusinessUnitId())
            .orElseThrow(() -> new RuntimeException("Business Unit not found with id: " + dto.getBusinessUnitId()));

        if (!(existingAccount.getBusinessUnit().getBusinessUnitId().equals(dto.getBusinessUnitId()))) {
            log.info("DTO BU does not match entity for draft account with ID: {}", draftAccountId);
            throw new ResourceConflictException(
                "DraftAccount",
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

        log.info(":replaceDraftAccount: Replacing draft account with ID: {} and new snapshot: \n{}",
                 draftAccountId, newSnapshot);

        return draftAccountRepository.save(existingAccount);
    }

    public DraftAccountEntity updateDraftAccount(Long draftAccountId, UpdateDraftAccountRequestDto dto)  {
        DraftAccountEntity existingAccount = draftAccountRepository.findById(draftAccountId)
            .orElseThrow(() -> new EntityNotFoundException("Draft Account not found with id: " + draftAccountId));

        if (!(existingAccount.getBusinessUnit().getBusinessUnitId().equals(dto.getBusinessUnitId()))) {
            log.info("DTO BU does not match entity for draft account with ID: {}", draftAccountId);
            throw new ResourceConflictException(
                "DraftAccount",
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

        if (newStatus == DraftAccountStatus.PENDING) {
            existingAccount.setValidatedDate(LocalDateTime.now());
            existingAccount.setValidatedBy(dto.getValidatedBy());
            existingAccount.setAccountSnapshot(addSnapshotApprovedDate(existingAccount));
        }
        // Set the timeline data as received from the front end
        existingAccount.setTimelineData(dto.getTimelineData());

        log.info(":updateDraftAccount: Updating draft account with ID: {} and status: {}",
                 draftAccountId, existingAccount.getAccountStatus());

        return draftAccountRepository.save(existingAccount);
    }

    private String addSnapshotApprovedDate(DraftAccountEntity existingAccount) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = (ObjectNode) mapper.readTree(existingAccount.getAccountSnapshot());

            String approvedDate = existingAccount.getValidatedDate()
                .atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            rootNode.put("approved_date", approvedDate);

            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON in addSnapshotApprovedDate", e);
        }
    }

    private String createInitialSnapshot(AddDraftAccountRequestDto dto, LocalDateTime created,
                                         BusinessUnitEntity businessUnit) {
        return buildSnapshot(dto.getAccount(), created, businessUnit, dto.getSubmittedBy(), dto.getSubmittedByName())
            .toPrettyJson();
    }

    private String createUpdateSnapshot(ReplaceDraftAccountRequestDto dto, LocalDateTime created,
                                         BusinessUnitEntity businessUnit) {
        return buildSnapshot(dto.getAccount(), created, businessUnit, dto.getSubmittedBy(), dto.getSubmittedByName())
            .toPrettyJson();
    }

    private  DraftAccountSnapshots.Snapshot buildSnapshot(String document, LocalDateTime created,
                                                          BusinessUnitEntity businessUnit, String submittedBy,
                                                          String submittedByName) {

        JsonPathUtil.DocContext docContext = createDocContext(document);

        String companyName = docContext.read(DEFENDANT_JSON_PATH + ".company_name");

        final boolean notCompany = companyName == null || companyName.isBlank();

        String defendantName = notCompany
            ? docContext.read(DEFENDANT_JSON_PATH + ".surname") + ", "
            + docContext.read(DEFENDANT_JSON_PATH + ".forenames")
            : companyName;

        String dob = notCompany
            ? docContext.read(DEFENDANT_JSON_PATH + ".dob")
            : null;
        String accType = docContext.read(ACCOUNT_JSON_PATH + ".account_type");

        return DraftAccountSnapshots.Snapshot.builder()
            .defendantName(defendantName)
            .dateOfBirth(dob)
            .createdDate(created.atOffset(ZoneOffset.UTC))
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
            .build();
    }
}
