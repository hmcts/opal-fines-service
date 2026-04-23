package uk.gov.hmcts.opal.service.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.repository.EnforcementRepository;

import java.util.Optional;

@Service
@Slf4j(topic = "opal.EnforcementRepositoryService")
@RequiredArgsConstructor
public class EnforcementRepositoryService {

    private final EnforcementRepository enforcementRepository;

    @Transactional(readOnly = true)
    public Optional<EnforcementEntity> getEnforcementMostRecent(Long defendantAccountId, String lastEnforcement) {
        return enforcementRepository.findFirstByDefendantAccountIdAndResultIdOrderByPostedDateDesc(
            defendantAccountId, lastEnforcement);
    }
}
