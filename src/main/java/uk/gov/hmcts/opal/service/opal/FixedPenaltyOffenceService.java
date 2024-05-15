package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.FixedPenaltyOffenceSearchDto;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.repository.FixedPenaltyOffenceRepository;
import uk.gov.hmcts.opal.repository.jpa.FixedPenaltyOffenceSpecs;
import uk.gov.hmcts.opal.service.FixedPenaltyOffenceServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("fixedPenaltyOffenceService")
public class FixedPenaltyOffenceService implements FixedPenaltyOffenceServiceInterface {

    private final FixedPenaltyOffenceRepository fixedPenaltyOffenceRepository;

    private final FixedPenaltyOffenceSpecs specs = new FixedPenaltyOffenceSpecs();

    @Override
    public FixedPenaltyOffenceEntity getFixedPenaltyOffence(long fixedPenaltyOffenceId) {
        return fixedPenaltyOffenceRepository.getReferenceById(fixedPenaltyOffenceId);
    }

    @Override
    public List<FixedPenaltyOffenceEntity> searchFixedPenaltyOffences(FixedPenaltyOffenceSearchDto criteria) {
        Page<FixedPenaltyOffenceEntity> page = fixedPenaltyOffenceRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
