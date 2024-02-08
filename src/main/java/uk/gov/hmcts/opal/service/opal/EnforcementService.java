package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.EnforcementSearchDto;
import uk.gov.hmcts.opal.entity.EnforcementEntity;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.repository.jpa.EnforcementSpecs;
import uk.gov.hmcts.opal.service.EnforcementServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnforcementService implements EnforcementServiceInterface {

    private final EnforcementRepository enforcementRepository;

    private final EnforcementSpecs specs = new EnforcementSpecs();

    @Override
    public EnforcementEntity getEnforcement(long enforcementId) {
        return enforcementRepository.getReferenceById(enforcementId);
    }

    @Override
    public List<EnforcementEntity> searchEnforcements(EnforcementSearchDto criteria) {
        Page<EnforcementEntity> page = enforcementRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
