package uk.gov.hmcts.opal.disco.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.TillSearchDto;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.repository.jpa.TillSpecs;
import uk.gov.hmcts.opal.disco.TillServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("tillService")
public class TillService implements TillServiceInterface {

    private final TillRepository tillRepository;

    private final TillSpecs specs = new TillSpecs();

    @Override
    public TillEntity getTill(long tillId) {
        return tillRepository.getReferenceById(tillId);
    }

    @Override
    public List<TillEntity> searchTills(TillSearchDto criteria) {
        Page<TillEntity> page = tillRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
