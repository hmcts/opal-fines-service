package uk.gov.hmcts.opal.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.opal.entity.SuspenseItemEntity;
import uk.gov.hmcts.opal.entity.SuspenseTransactionEntity;

public interface SuspenseTransactionRepository extends JpaRepository<SuspenseTransactionEntity, Long> {

    List<SuspenseTransactionEntity> findAllBySuspenseItem(SuspenseItemEntity suspenseItem);
}
