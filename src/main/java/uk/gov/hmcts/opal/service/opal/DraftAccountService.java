package uk.gov.hmcts.opal.service.opal;


import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.DraftAccountSnapshotsDto;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.DraftAccountStatus;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.DraftAccountSpecs;
import uk.gov.hmcts.opal.service.DraftAccountServiceInterface;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Slf4j(topic = "DraftAccountService")
@RequiredArgsConstructor
@Qualifier("draftAccountService")
public class DraftAccountService implements DraftAccountServiceInterface {

    private final DraftAccountRepository draftAccountRepository;

    private final BusinessUnitRepository businessUnitRepository;

    private final DraftAccountSpecs specs = new DraftAccountSpecs();

    @Override
    public DraftAccountEntity getDraftAccount(long draftAccountId) {
        return draftAccountRepository.getReferenceById(draftAccountId);
    }

    @Override
    public List<DraftAccountEntity> searchDraftAccounts(DraftAccountSearchDto criteria) {
        Page<DraftAccountEntity> page = draftAccountRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

    public DraftAccountEntity submitDraftAccount(AddDraftAccountRequestDto dto, String userName) {
        LocalDateTime created = LocalDateTime.now();
        BusinessUnitEntity businessUnit = businessUnitRepository.findById(dto.getBusinessUnitId()).orElse(null);
        String snapshot = createInitialSnapshot(dto, created, businessUnit, userName);
        log.info(":submitDraftAccount: snapshot: \n{}", snapshot);
        return draftAccountRepository.save(toEntity(dto, created, businessUnit, userName, snapshot));
    }

    private String createInitialSnapshot(AddDraftAccountRequestDto dto, LocalDateTime created,
                                         BusinessUnitEntity businessUnit, String userName) {
        return buildInitialSnapshot(dto.getAccount(), created, businessUnit, userName).toPrettyJson();
    }

    private  DraftAccountSnapshotsDto.Snapshot  buildInitialSnapshot(String document, LocalDateTime created,
                                      BusinessUnitEntity businessUnit, String userName) {

        Configuration config = Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);
        DocumentContext docContext = JsonPath.parse(document, config);

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

        return DraftAccountSnapshotsDto.Snapshot.builder()
            .defendantName(defendantName)
            .dateOfBirth(dob)
            .createdDate(created.atOffset(ZoneOffset.UTC))
            .accountType(accType)
            .submittedBy(userName)
            .businessUnitName(businessUnit.getBusinessUnitName())
            .build();
    }

    DraftAccountEntity toEntity(AddDraftAccountRequestDto dto, LocalDateTime created,
                                BusinessUnitEntity businessUnit, String userName, String snapshot) {
        return DraftAccountEntity.builder()
            .businessUnit(businessUnit)
            .createdDate(created)
            .submittedBy(userName)
            .account(dto.getAccount())
            .accountSnapshot(snapshot)
            .accountType(dto.getAccountType())
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .timelineData(dto.getTimelineData())
            .build();
    }
}
