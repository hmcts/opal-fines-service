package uk.gov.hmcts.opal.service.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountPartiesRepository;

@Service
@Slf4j(topic = "opal.DefendantAccountPartiesRepositoryService")
@RequiredArgsConstructor
public class DefendantAccountPartiesRepositoryService {

    private final DefendantAccountPartiesRepository repository;

    @Transactional
    public void delete(DefendantAccountPartiesEntity entity) {
        if (entity == null) {
            return;
        }
        log.debug("Deleting DefendantAccountPartiesEntity with id: {}", entity.getDefendantAccountPartyId());
        repository.delete(entity);
    }
}

