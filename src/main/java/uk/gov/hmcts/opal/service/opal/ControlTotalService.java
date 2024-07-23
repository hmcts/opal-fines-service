package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ControlTotalSearchDto;
import uk.gov.hmcts.opal.entity.ControlTotalEntity;
import uk.gov.hmcts.opal.repository.ControlTotalRepository;
import uk.gov.hmcts.opal.repository.jpa.ControlTotalSpecs;
import uk.gov.hmcts.opal.service.ControlTotalServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("controlTotalService")
public class ControlTotalService implements ControlTotalServiceInterface {

    private final ControlTotalRepository controlTotalRepository;

    private final ControlTotalSpecs specs = new ControlTotalSpecs();

    @Override
    public ControlTotalEntity getControlTotal(long controlTotalId) {
        return controlTotalRepository.getReferenceById(controlTotalId);
    }

    @Override
    public List<ControlTotalEntity> searchControlTotals(ControlTotalSearchDto criteria) {
        Page<ControlTotalEntity> page = controlTotalRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
