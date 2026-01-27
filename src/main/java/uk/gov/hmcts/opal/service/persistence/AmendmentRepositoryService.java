package uk.gov.hmcts.opal.service.persistence;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.search.AmendmentSearchDto;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.repository.AmendmentRepository;
import uk.gov.hmcts.opal.repository.jpa.AmendmentSpecs;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.AmendmentRepositoryService")
public class AmendmentRepositoryService {

    private final AmendmentRepository amendmentRepository;

    private final AmendmentSpecs specs = new AmendmentSpecs();

    @Transactional(readOnly = true)
    public AmendmentEntity findById(long amendmentId) {
        return amendmentRepository.findById(amendmentId)
            .orElseThrow(() -> new EntityNotFoundException("Amendment not found with id: " + amendmentId));
    }

    public Page<AmendmentEntity> getAmendmentsByCriteriaAsPage(AmendmentSearchDto criteria, Sort dateSort) {

        return amendmentRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq
                        .sortBy(dateSort)
                        .page(Pageable.unpaged()));
    }

    @Transactional
    public void auditInitialiseStoredProc(Long accountId, RecordType recordType) {
        amendmentRepository.auditInitialise(accountId, recordType.getType());
    }

    @Transactional
    public void auditFinaliseStoredProc(Long accountId, RecordType recordType,
                                        Short businessUnitId, String postedBy, String caseRef, String functionCode) {
        amendmentRepository
            .auditFinalise(accountId, recordType.getType(), businessUnitId, postedBy, caseRef, functionCode);
    }

}
