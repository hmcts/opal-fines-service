package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.OffenseSearchDto;
import uk.gov.hmcts.opal.entity.OffenseEntity;
import uk.gov.hmcts.opal.repository.OffenseRepository;
import uk.gov.hmcts.opal.repository.jpa.OffenseSpecs;
import uk.gov.hmcts.opal.service.OffenseServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("offenseService")
public class OffenseService implements OffenseServiceInterface {

    private final OffenseRepository offenseRepository;

    private final OffenseSpecs specs = new OffenseSpecs();

    @Override
    public OffenseEntity getOffense(long offenseId) {
        return offenseRepository.getReferenceById(offenseId);
    }

    @Override
    public List<OffenseEntity> searchOffenses(OffenseSearchDto criteria) {
        Page<OffenseEntity> page = offenseRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
