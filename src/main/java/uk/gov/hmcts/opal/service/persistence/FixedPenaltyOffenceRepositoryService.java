package uk.gov.hmcts.opal.service.persistence;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.repository.FixedPenaltyOffenceRepository;

@Service
@Slf4j(topic = "opal.FixedPenaltyOffenceRepositoryService")
@RequiredArgsConstructor
public class FixedPenaltyOffenceRepositoryService {

    private final FixedPenaltyOffenceRepository fixedPenaltyOffenceRepository;

    public FixedPenaltyOffenceEntity findByDefendantAccountId(
        Long defendantAccountId) {
        return fixedPenaltyOffenceRepository.findByDefendantAccountId(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException(
            "Fixed Penalty Offence not found for account: " + defendantAccountId));
    }

}
