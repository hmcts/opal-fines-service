package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.EnforcerSearchDto;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.repository.EnforcerRepository;
import uk.gov.hmcts.opal.repository.jpa.EnforcerSpecs;
import uk.gov.hmcts.opal.service.EnforcerServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("enforcerService")
public class EnforcerService implements EnforcerServiceInterface {

    private final EnforcerRepository enforcerRepository;

    private final EnforcerSpecs specs = new EnforcerSpecs();

    @Override
    public EnforcerEntity getEnforcer(long enforcerId) {
        return enforcerRepository.getReferenceById(enforcerId);
    }

    @Override
    public List<EnforcerEntity> searchEnforcers(EnforcerSearchDto criteria) {
        Page<EnforcerEntity> page = enforcerRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
