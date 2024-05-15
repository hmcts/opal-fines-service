package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.AmendmentSearchDto;
import uk.gov.hmcts.opal.entity.AmendmentEntity;
import uk.gov.hmcts.opal.repository.AmendmentRepository;
import uk.gov.hmcts.opal.repository.jpa.AmendmentSpecs;
import uk.gov.hmcts.opal.service.AmendmentServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("amendmentService")
public class AmendmentService implements AmendmentServiceInterface {

    private final AmendmentRepository amendmentRepository;

    private final AmendmentSpecs specs = new AmendmentSpecs();

    @Override
    public AmendmentEntity getAmendment(long amendmentId) {
        return amendmentRepository.getReferenceById(amendmentId);
    }

    @Override
    public List<AmendmentEntity> searchAmendments(AmendmentSearchDto criteria) {
        Page<AmendmentEntity> page = amendmentRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
