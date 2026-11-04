package uk.gov.hmcts.opal.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.SuspenseItemEntity;

@Repository
public interface SuspenseItemRepository extends JpaRepository<SuspenseItemEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SuspenseItemEntity> findBySuspenseItemId(Long suspenseItemId);
}
