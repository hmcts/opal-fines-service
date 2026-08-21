package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.DocumentInstanceEntity;

@Repository
public interface DocumentInstanceRepository extends JpaRepository<DocumentInstanceEntity, Long> {

    @Modifying
    @Query(
        value = """
            delete from document_instances
            where associated_record_type = cast(:associatedRecordType as t_associated_record_type_enum)
              and associated_record_id = :associatedRecordId
            """,
        nativeQuery = true
    )
    void deleteByAssociatedRecordTypeAndAssociatedRecordId(
        @Param("associatedRecordType") String associatedRecordType,
        @Param("associatedRecordId") String associatedRecordId
    );

    @Query(
        value = """
            select count(*)
            from document_instances
            where associated_record_type = cast(:associatedRecordType as t_associated_record_type_enum)
              and associated_record_id = :associatedRecordId
            """,
        nativeQuery = true
    )
    long countByAssociatedRecordTypeAndAssociatedRecordId(
        @Param("associatedRecordType") String associatedRecordType,
        @Param("associatedRecordId") String associatedRecordId
    );
}
