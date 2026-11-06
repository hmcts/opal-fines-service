package uk.gov.hmcts.opal.repository;

import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.opal.entity.ControlTotalEntity;

public interface ControlTotalRepository extends JpaRepository<ControlTotalEntity, Long> {

    @Modifying
    @Query("""
        DELETE FROM ControlTotalEntity controlTotal
         WHERE CAST(controlTotal.associatedRecordType AS string) = :associatedRecordType
           AND controlTotal.associatedRecordId IN :associatedRecordIds
        """)
    long deleteAllByAssociatedRecordTypeAndAssociatedRecordIdIn(
        @Param("associatedRecordType") String associatedRecordType,
        @Param("associatedRecordIds") Collection<String> associatedRecordIds);
}
