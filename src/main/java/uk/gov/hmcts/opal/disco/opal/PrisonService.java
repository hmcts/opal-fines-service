package uk.gov.hmcts.opal.disco.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.PrisonSearchDto;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.repository.PrisonRepository;
import uk.gov.hmcts.opal.repository.jpa.PrisonSpecs;
import uk.gov.hmcts.opal.disco.PrisonServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("prisonService")
public class PrisonService implements PrisonServiceInterface {

    private final PrisonRepository prisonRepository;

    private final PrisonSpecs specs = new PrisonSpecs();

    @Override
    public PrisonEntity getPrison(long prisonId) {
        return prisonRepository.getReferenceById(prisonId);
    }

    @Override
    public List<PrisonEntity> searchPrisons(PrisonSearchDto criteria) {
        Page<PrisonEntity> page = prisonRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
