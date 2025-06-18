package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.DefendantTransactionEntity;

import java.util.List;

@Repository
public interface DefendantTransactionRepository extends JpaRepository<DefendantTransactionEntity, Long>,
    JpaSpecificationExecutor<DefendantTransactionEntity> {
    void deleteByDefendantAccount_DefendantAccountId(long defendantAccountId);

    @Query("SELECT dt.defendantTransactionId FROM DefendantTransactionEntity dt "
        + "WHERE dt.defendantAccount.defendantAccountId = :accountId")
    List<Long> findDefendantAccountTransactionIdsByDefendantAccountId(@Param("accountId")long defendantAccountId);

}
