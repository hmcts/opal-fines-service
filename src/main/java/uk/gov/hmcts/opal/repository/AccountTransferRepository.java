package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.AccountTransferEntity;

@Repository
public interface AccountTransferRepository extends JpaRepository<AccountTransferEntity, Long>,
    JpaSpecificationExecutor<AccountTransferEntity> {

    AccountTransferEntity findByAccountTransferId(Long accountTransferId);
}
