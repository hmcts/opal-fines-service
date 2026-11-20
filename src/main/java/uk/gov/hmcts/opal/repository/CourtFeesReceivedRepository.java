package uk.gov.hmcts.opal.repository;

import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.CourtFeesReceivedEntity;
import uk.gov.hmcts.opal.entity.SuspenseTransactionEntity;

@Repository
public interface CourtFeesReceivedRepository extends JpaRepository<CourtFeesReceivedEntity, Long> {

    long deleteAllBySuspenseTransactionIn(Collection<SuspenseTransactionEntity> suspenseTransactions);


}
