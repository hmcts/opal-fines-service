package uk.gov.hmcts.opal.repository;

import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.TillEntity;

@Repository
public interface TillRepository extends JpaRepository<TillEntity, Long> {

    long countByInterfaceFile_InterfaceFileIdIn(Collection<Long> interfaceFileIds);

    // Till numbers are allocated from legacy sequences named per business unit, e.g. till_number_10_seq.
    @Query(value = """
        SELECT nextval(('till_number_' || CAST(:businessUnitId AS text) || '_seq')::regclass)
        """, nativeQuery = true)
    Long getNextTillNumber(@Param("businessUnitId") Short businessUnitId);
}
