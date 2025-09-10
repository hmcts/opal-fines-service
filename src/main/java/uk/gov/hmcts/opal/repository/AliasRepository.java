package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.AliasEntity;

import java.util.List;

@Repository
public interface AliasRepository extends JpaRepository<AliasEntity, Long> {
    List<AliasEntity> findByParty_PartyId(Long partyId);
}
