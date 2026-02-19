package uk.gov.hmcts.opal.repository;

import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditorAccountRepository extends JpaRepository<CreditorAccountEntity.Lite, Long>,
    JpaSpecificationExecutor<CreditorAccountEntity.Lite> {

    long countByMinorCreditorPartyId(Long partyId);
}
