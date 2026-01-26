package uk.gov.hmcts.opal.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;

@Repository
public interface DefendantAccountRepository extends JpaRepository<DefendantAccountEntity, Long>,
        JpaSpecificationExecutor<DefendantAccountEntity> {

    DefendantAccountEntity findByBusinessUnit_BusinessUnitIdAndAccountNumber(Short businessUnitId,
                                                                               String accountNumber);

    List<DefendantAccountEntity> findAllByBusinessUnit_BusinessUnitId(Short businessUnitId);

    Optional<DefendantAccountEntity> findByDefendantAccountId(Long defendantAccountId);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select d from DefendantAccountEntity d where d.defendantAccountId = :id")
    Optional<DefendantAccountEntity> findByDefendantAccountIdForUpdate(@Param("id") Long defendantAccountId);
}
