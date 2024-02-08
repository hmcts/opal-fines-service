package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.EnforcementSearchDto;
import uk.gov.hmcts.opal.entity.EnforcementEntity;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.service.EnforcementServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnforcementService implements EnforcementServiceInterface {

    private final EnforcementRepository enforcementRepository;

    @Override
    public EnforcementEntity getEnforcement(long enforcementId) {
        return enforcementRepository.getReferenceById(enforcementId);
    }

    @Override
    public List<EnforcementEntity> searchEnforcements(EnforcementSearchDto criteria) {
        return null;
    }

}
