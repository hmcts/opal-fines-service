package uk.gov.hmcts.opal.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;

import java.util.Optional;

@Repository
public interface DraftAccountRepository extends JpaRepository<DraftAccountEntity, Long>,
    JpaSpecificationExecutor<DraftAccountEntity> {

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    Optional<DraftAccountEntity> findWithOptimisticLockByDraftAccountId(Long id);

    @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    Optional<DraftAccountEntity> findWithPessimisticForceIncByDraftAccountId(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DraftAccountEntity> findWithPessimisticWriteByDraftAccountId(Long id);

}
