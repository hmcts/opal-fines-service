package uk.gov.hmcts.opal.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity.Lite;

@Repository
public interface CreditorAccountRepository extends JpaRepository<CreditorAccountEntity.Lite, Long>,
    JpaSpecificationExecutor<CreditorAccountEntity.Lite> {

    Optional<Lite> findByCreditorAccountIdAndBusinessUnitId(
        Long creditorAccountId,
        Short businessUnitId
    );
}
