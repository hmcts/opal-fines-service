package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.AliasEntity;

import java.util.List;

@Repository
public interface AliasRepository extends JpaRepository<AliasEntity, Long> {
    @Query("""
        select a
        from AliasEntity a
        join DefendantAccountPartiesEntity dap on dap.party.partyId = a.party.partyId
        join DefendantAccountEntity da on da.defendantAccountId = dap.defendantAccount.defendantAccountId
        where da.defendantAccountId = :defendantAccountId
        order by a.sequenceNumber
        """)
    List<AliasEntity> findAllByDefendantAccountId(@Param("defendantAccountId") Long defendantAccountId);
}
