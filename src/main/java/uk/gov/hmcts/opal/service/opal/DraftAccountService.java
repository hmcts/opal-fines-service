package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.DraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.ReplaceDraftAccountRequestDto;
import uk.gov.hmcts.opal.entity.DraftAccountSnapshots;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.DraftAccountStatus;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.DraftAccountSpecs;
import uk.gov.hmcts.opal.util.JsonPathUtil;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.opal.util.JsonPathUtil.createDocContext;

@Service
@Slf4j(topic = "DraftAccountService")
@RequiredArgsConstructor
@Qualifier("draftAccountService")
public class DraftAccountService {

    private final DraftAccountRepository draftAccountRepository;

    private final BusinessUnitRepository businessUnitRepository;

    private final DraftAccountSpecs specs = new DraftAccountSpecs();

    public DraftAccountEntity getDraftAccount(long draftAccountId) {
        return draftAccountRepository.getReferenceById(draftAccountId);
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
        log.info(":submitDraftAccount: snapshot: \n{}", snapshot);
        return draftAccountRepository.save(toEntity(dto, created, businessUnit, snapshot));
    }

    public DraftAccountEntity replaceDraftAccount(Long draftAccountId, ReplaceDraftAccountRequestDto dto) {
        DraftAccountEntity existingAccount = draftAccountRepository.findById(draftAccountId)
            .orElseThrow(() -> new RuntimeException("Draft Account not found with id: " + draftAccountId));

        BusinessUnitEntity businessUnit = businessUnitRepository.findById(dto.getBusinessUnitId())
            .orElseThrow(() -> new RuntimeException("Business Unit not found with id: " + dto.getBusinessUnitId()));

        LocalDateTime updatedTime = LocalDateTime.now();
        String newSnapshot = createInitialSnapshot(dto, updatedTime, businessUnit);
        existingAccount.setSubmittedBy(dto.getSubmittedBy());
        existingAccount.setAccount(dto.getAccount());
        existingAccount.setAccountSnapshot(newSnapshot);
        existingAccount.setAccountType(dto.getAccountType());
        existingAccount.setAccountStatus(DraftAccountStatus.RESUBMITTED);
        existingAccount.setTimelineData(dto.getTimelineData());

        log.info(":replaceDraftAccount: Replacing draft account with ID: {} and new snapshot: \n{}",
                 draftAccountId, newSnapshot);

        return draftAccountRepository.save(existingAccount);
    }

    private String createInitialSnapshot(DraftAccountRequestDto dto, LocalDateTime created,
                                         BusinessUnitEntity businessUnit) {
        return buildInitialSnapshot(dto.getAccount(), created, businessUnit, dto.getSubmittedBy()).toPrettyJson();
    }

    private  DraftAccountSnapshots.Snapshot  buildInitialSnapshot(String document, LocalDateTime created,
                                                                  BusinessUnitEntity businessUnit, String userName) {

        JsonPathUtil.DocContext docContext = createDocContext(document);

        String companyName = docContext.read("$.accountCreateRequest.Defendant.CompanyName");

        final boolean notCompany = companyName == null || companyName.isBlank();

        String defendantName = notCompany
            ? docContext.read("$.accountCreateRequest.Defendant.Surname") + ", "
            + docContext.read("$.accountCreateRequest.Defendant.Forenames")
            : companyName;

        String dob = notCompany
            ? docContext.read("$.accountCreateRequest.Defendant.DOB")
            : null;
        String accType = docContext.read("$.accountCreateRequest.Account.AccountType");

        return DraftAccountSnapshots.Snapshot.builder()
            .defendantName(defendantName)
            .dateOfBirth(dob)
            .createdDate(created.atOffset(ZoneOffset.UTC))
            .accountType(accType)
            .submittedBy(userName)
            .businessUnitName(businessUnit.getBusinessUnitName())
            .build();
    }

    DraftAccountEntity toEntity(DraftAccountRequestDto dto, LocalDateTime created,
                                BusinessUnitEntity businessUnit, String snapshot) {
        return DraftAccountEntity.builder()
            .businessUnit(businessUnit)
            .createdDate(created)
            .submittedBy(dto.getSubmittedBy())
            .account(dto.getAccount())
            .accountSnapshot(snapshot)
            .accountType(dto.getAccountType())
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .timelineData(dto.getTimelineData())
            .build();
    }
}
