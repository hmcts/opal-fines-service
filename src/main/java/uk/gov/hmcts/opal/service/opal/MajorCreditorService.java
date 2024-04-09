package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.MajorCreditorSearchDto;
import uk.gov.hmcts.opal.entity.MajorCreditorEntity;
import uk.gov.hmcts.opal.repository.MajorCreditorRepository;
import uk.gov.hmcts.opal.repository.jpa.MajorCreditorSpecs;
import uk.gov.hmcts.opal.service.MajorCreditorServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("majorCreditorService")
public class MajorCreditorService implements MajorCreditorServiceInterface {

    private final MajorCreditorRepository majorCreditorRepository;

    private final MajorCreditorSpecs specs = new MajorCreditorSpecs();

    @Override
    public MajorCreditorEntity getMajorCreditor(long majorCreditorId) {
        return majorCreditorRepository.getReferenceById(majorCreditorId);
    }

    @Override
    public List<MajorCreditorEntity> searchMajorCreditors(MajorCreditorSearchDto criteria) {
        Page<MajorCreditorEntity> page = majorCreditorRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
