package uk.gov.hmcts.opal.service.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountPartiesRepository;

@Service
@Slf4j(topic = "opal.DefendantAccountPartiesRepositoryService")
@RequiredArgsConstructor
public class DefendantAccountPartiesRepositoryService {

    private final DefendantAccountPartiesRepository defendantAccountPartiesRepository;

    @Transactional
    public void deleteAndFlush(DefendantAccountPartiesEntity defendantAccountPartiesEntity) {
        log.debug("Deleting DefendantAccountPartiesEntity with id: {}",
            defendantAccountPartiesEntity.getDefendantAccountPartyId());
        defendantAccountPartiesRepository.delete(defendantAccountPartiesEntity);
        defendantAccountPartiesRepository.flush();
    }
}
