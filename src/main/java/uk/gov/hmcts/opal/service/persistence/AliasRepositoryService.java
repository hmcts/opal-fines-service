package uk.gov.hmcts.opal.service.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.repository.AliasRepository;

import java.util.List;
import java.util.Set;

@Service
@Slf4j(topic = "opal.AliasRepositoryService")
@RequiredArgsConstructor
public class AliasRepositoryService {

    private final AliasRepository aliasRepository;

    @Transactional(readOnly = true)
    public List<AliasEntity> findByPartyId(Long partyId) {
        log.debug("Finding AliasEntity by partyId: {}", partyId);
        return aliasRepository.findByParty_PartyId(partyId);
    }

    @Transactional
    public List<AliasEntity> saveAll(List<AliasEntity> aliases) {
        log.debug("Saving {} AliasEntity records", aliases.size());
        return aliasRepository.saveAll(aliases);
    }

    public void flush() {
        log.debug("Flushing AliasRepository");
        aliasRepository.flush();
    }

    @Transactional
    public void deleteByPartyId(Long partyId) {
        log.debug("Deleting AliasEntity records by partyId: {}", partyId);
        aliasRepository.deleteByParty_PartyId(partyId);
    }

    @Transactional
    public void deleteByPartyIdNotIn(Long partyId, Set<Long> aliasIds) {
        log.debug("Deleting AliasEntity records by partyId: {} not in aliasIds: {}", partyId, aliasIds);
        aliasRepository.deleteByParty_PartyIdAndAliasIdNotIn(partyId, aliasIds);
    }
}
