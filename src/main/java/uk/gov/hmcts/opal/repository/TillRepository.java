package uk.gov.hmcts.opal.repository;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.TillEntity;

@Repository
public interface TillRepository extends JpaRepository<TillEntity, Long> {

    long countByInterfaceFile_InterfaceFileIdIn(Collection<Long> interfaceFileIds);

    /**
     * Locks tills before eligibility checks so concurrent allocation requests cannot both enqueue the same till.
     * Tills have no version field for optimistic locking; these locks last until the caller's transaction ends.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<TillEntity> findAllByTillIdInOrderByTillId(Collection<Long> tillIds);

    // Till numbers are allocated from legacy sequences named per business unit, e.g. till_number_10_seq.
    @Query(value = """
        SELECT nextval(('till_number_' || CAST(:businessUnitId AS text) || '_seq')::regclass)
        """, nativeQuery = true)
    Long getNextTillNumber(@Param("businessUnitId") Short businessUnitId);
}
