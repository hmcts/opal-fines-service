package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.EnforcerSearchDto;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.repository.EnforcerRepository;
import uk.gov.hmcts.opal.service.EnforcerServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnforcerService implements EnforcerServiceInterface {

    private final EnforcerRepository enforcerRepository;

    @Override
    public EnforcerEntity getEnforcer(long enforcerId) {
        return enforcerRepository.getReferenceById(enforcerId);
    }

    @Override
    public List<EnforcerEntity> searchEnforcers(EnforcerSearchDto criteria) {
        return null;
    }

}
