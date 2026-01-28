package uk.gov.hmcts.opal.service.persistence;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.repository.PartyRepository;

@Service
@Slf4j(topic = "opal.PartyRepositoryService")
@RequiredArgsConstructor
public class PartyRepositoryService {

    private final PartyRepository repository;

    public PartyEntity findById(Long partyId) {
        log.debug("Finding PartyEntity by partyId: {}", partyId);

        return repository.findById(partyId)
            .orElseThrow(() -> new EntityNotFoundException("Party not found with id: " + partyId));

    }

    @Transactional
    public PartyEntity save(PartyEntity party) {
        log.debug("Saving PartyEntity with partyId: {}", party.getPartyId());
        return repository.save(party);
    }

    @Transactional
    public PartyEntity updateById(Long partyId, UnaryOperator<PartyEntity> mutator) {
        PartyEntity existing = findById(partyId);
        PartyEntity updated = mutator.apply(existing);
        return save(updated);
    }

}
