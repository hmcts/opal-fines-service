package uk.gov.hmcts.opal.disco.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ImpositionSearchDto;
import uk.gov.hmcts.opal.entity.ImpositionEntity;
import uk.gov.hmcts.opal.repository.ImpositionRepository;
import uk.gov.hmcts.opal.repository.jpa.ImpositionSpecs;
import uk.gov.hmcts.opal.disco.ImpositionServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("impositionService")
public class ImpositionService implements ImpositionServiceInterface {

    private final ImpositionRepository impositionRepository;

    private final ImpositionSpecs specs = new ImpositionSpecs();

    @Override
    public ImpositionEntity getImposition(long impositionId) {
        return impositionRepository.getReferenceById(impositionId);
    }

    @Override
    public List<ImpositionEntity> searchImpositions(ImpositionSearchDto criteria) {
        Page<ImpositionEntity> page = impositionRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
