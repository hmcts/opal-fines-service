package uk.gov.hmcts.opal.service.opal;


import uk.gov.hmcts.opal.dto.response.SearchDataResponse;
import uk.gov.hmcts.opal.dto.search.AmendmentSearchDto;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity_;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.repository.AmendmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.service.persistence.AmendmentRepositoryService;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.AmendmentService")
@Qualifier("amendmentService")
public class AmendmentService {

    private final AmendmentRepositoryService amendmentRepositoryService;
    private final AmendmentRepository amendmentRepository;

    @Transactional(readOnly = true)
    public SearchDataResponse<AmendmentEntity> searchAmendments(AmendmentSearchDto criteria) {
        log.info(":searchAmendments: criteria: {}", criteria);

        Sort dateSort = Sort.by(Sort.Direction.DESC, AmendmentEntity_.AMENDED_DATE);

        Page<AmendmentEntity> page = amendmentRepositoryService.getAmendmentsByCriteriaAsPage(criteria, dateSort);

        return SearchDataResponse.<AmendmentEntity>builder()
            .searchData(page.getContent())
            .build();
    }

    @Transactional(readOnly = true)
    public AmendmentEntity getAmendmentById(Long amendmentId) {
        log.info(":getAmendmentById: amendmentId: {}", amendmentId);

        return amendmentRepositoryService.findById(amendmentId);
    }

    //TODO remove in favour of repository service method
    @Transactional
    public void auditInitialiseStoredProc(Long accountId, RecordType recordType) {
        amendmentRepository.auditInitialise(accountId, recordType.getType());
    }

    //TODO remove in favour of repository service method
    @Transactional
    public void auditFinaliseStoredProc(Long accountId, RecordType recordType,
                                        Short businessUnitId, String postedBy, String caseRef, String functionCode) {
        amendmentRepository
            .auditFinalise(accountId, recordType.getType(), businessUnitId, postedBy, caseRef, functionCode);
    }

}
