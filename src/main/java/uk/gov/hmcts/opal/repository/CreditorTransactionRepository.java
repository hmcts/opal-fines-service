package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity;

@Repository
public interface CreditorTransactionRepository extends JpaRepository<CreditorTransactionEntity, Long>,
    JpaSpecificationExecutor<CreditorTransactionEntity> {
}
