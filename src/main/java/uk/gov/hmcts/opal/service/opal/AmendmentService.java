package uk.gov.hmcts.opal.service.opal;


import uk.gov.hmcts.opal.dto.response.SearchDataResponse;
import uk.gov.hmcts.opal.dto.search.AmendmentSearchDto;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity_;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.repository.AmendmentRepository;
import uk.gov.hmcts.opal.repository.jpa.AmendmentSpecs;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.AmendmentService")
@Qualifier("amendmentService")
public class AmendmentService {

    private final AmendmentRepository amendmentRepository;

    private final AmendmentSpecs specs = new AmendmentSpecs();

    public AmendmentEntity getAmendmentById(long amendmentId) {
        return amendmentRepository.findById(amendmentId)
            .orElseThrow(() -> new EntityNotFoundException("Amendment not found with id: " + amendmentId));
    }

    public SearchDataResponse<AmendmentEntity> searchAmendments(AmendmentSearchDto criteria) {
        log.info(":searchAmendments: criteria: {}", criteria);

        Sort dateSort = Sort.by(Sort.Direction.DESC, AmendmentEntity_.AMENDED_DATE);

        Page<AmendmentEntity> page = amendmentRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq
                        .sortBy(dateSort)
                        .page(Pageable.unpaged()));

        return SearchDataResponse.<AmendmentEntity>builder()
            .searchData(page.getContent())
            .build();
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
