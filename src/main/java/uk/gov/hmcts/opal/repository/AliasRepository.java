package uk.gov.hmcts.opal.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.AliasEntity;

@Repository
public interface AliasRepository extends JpaRepository<AliasEntity, Long>, JpaSpecificationExecutor<AliasEntity> {

    List<AliasEntity> findByParty_PartyId(Long partyId);

    void deleteByParty_PartyId(Long partyId);

    void deleteByParty_PartyIdAndAliasIdNotIn(Long partyId, Collection<Long> keepIds);
}
