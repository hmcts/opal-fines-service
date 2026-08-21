package uk.gov.hmcts.opal.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity;
import uk.gov.hmcts.opal.repository.projection.MinorCreditorAmendmentHistoryProjection;

@Repository
public interface AmendmentRepository extends JpaRepository<AmendmentEntity, Long>,
    JpaSpecificationExecutor<AmendmentEntity> {

    String ASSOC_ACCOUNT_ID = "pi_associated_account_id";
    String RECORD_TYPE = "pi_record_type";
    String BUSINESS_UNIT_ID = "pi_business_unit_id";
    String POSTED_BY = "pi_posted_by";
    String POSTED_BY_NAME = "pi_posted_by_name";
    String CASE_REFERENCE = "pi_case_reference";
    String FUNCTION_CODE = "pi_function_code";


    @Modifying
    @Query(
        nativeQuery = true,
        value = """
            CALL p_audit_initialise(
                :pi_associated_account_id,
                CAST(:pi_record_type AS t_associated_record_type_enum)
            )
        """
    )
    void auditInitialise(@Param(ASSOC_ACCOUNT_ID) Long accountId, @Param(RECORD_TYPE) String associatedRecordType);

    @Modifying
    @Query(
        nativeQuery = true,
        value = """
            CALL p_audit_finalise(
                        :pi_associated_account_id,
                        CAST(:pi_record_type AS t_associated_record_type_enum),
                        :pi_business_unit_id,
                        :pi_posted_by,
                        :pi_posted_by_name,
                        :pi_case_reference,
                        :pi_function_code
                    )
        """
    )
    void auditFinalise(@Param(ASSOC_ACCOUNT_ID) Long accountId, @Param(RECORD_TYPE) String associatedRecordType,
        @Param(BUSINESS_UNIT_ID) Short businessUnitId, @Param(POSTED_BY) String postedBy,
        @Param(POSTED_BY_NAME) String postedByName, @Param(CASE_REFERENCE) String caseRef,
        @Param(FUNCTION_CODE) String functionCode);

    void deleteByAssociatedRecordId(String defendantAccountId);

    int countByAssociatedRecordId(String associatedRecordId);

    AmendmentEntity findFirstByAssociatedRecordIdOrderByAmendmentIdDesc(String associatedRecordId);

    List<AmendmentEntity> findByAssociatedRecordIdOrderByAmendmentIdAsc(String associatedRecordId);

    @Query(value = """
        SELECT a.amendment_id AS amendmentId,
               a.amended_date AS postedDate,
               a.amended_by AS postedBy,
               a.amended_by_name AS postedByName,
               aaf.data_item AS attributeName,
               a.old_value AS oldValue,
               a.new_value AS newValue
          FROM amendments a
          JOIN audit_amendment_fields aaf
            ON aaf.field_code = a.field_code
         WHERE a.associated_record_type = 'creditor_accounts'
           AND a.associated_record_id = :creditorAccountId
           AND a.amended_date >= :postedFromInclusive
           AND a.amended_date < :postedToExclusive
         ORDER BY a.amended_date DESC, a.amendment_id
        """, nativeQuery = true)
    List<MinorCreditorAmendmentHistoryProjection> findMinorCreditorHistory(
        @Param("creditorAccountId") String creditorAccountId,
        @Param("postedFromInclusive") LocalDateTime postedFromInclusive,
        @Param("postedToExclusive") LocalDateTime postedToExclusive);
}
