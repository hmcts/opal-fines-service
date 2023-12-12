package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;

@Repository
public interface DebtorDetailRepository extends JpaRepository<DebtorDetailEntity, Long> {
    DebtorDetailEntity findByParty_PartyId(PartyEntity party);
}
