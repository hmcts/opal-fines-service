package uk.gov.hmcts.opal.repository;

import uk.gov.hmcts.opal.entity.CreditorTransactionEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditorTransactionRepository extends JpaRepository<CreditorTransactionEntity, Long>,
    JpaSpecificationExecutor<CreditorTransactionEntity> {

}
