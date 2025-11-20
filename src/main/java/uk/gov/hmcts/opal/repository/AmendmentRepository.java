package uk.gov.hmcts.opal.repository;

import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AmendmentRepository extends JpaRepository<AmendmentEntity, Long>,
    JpaSpecificationExecutor<AmendmentEntity> {

    String DB_PROC_INITIALISE_NAME = "p_audit_initialise";
    String DB_PROC_FINALISE_NAME = "p_audit_finalise";
    String ASSOC_ACCOUNT_ID = "pi_associated_account_id";
    String RECORD_TYPE = "pi_record_type";
    String BUSINESS_UNIT_ID = "pi_business_unit_id";
    String POSTED_BY = "pi_posted_by";
    String CASE_REFERENCE = "pi_case_reference";
    String FUNCTION_CODE = "pi_function_code";


    @Procedure(procedureName = DB_PROC_INITIALISE_NAME)
    void auditInitialise(@Param(ASSOC_ACCOUNT_ID) Long accountId, @Param(RECORD_TYPE) String recordType);

    @Procedure(procedureName = DB_PROC_FINALISE_NAME)
    void auditFinalise(@Param(ASSOC_ACCOUNT_ID) Long accountId, @Param(RECORD_TYPE) String recordType,
                       @Param(BUSINESS_UNIT_ID) Short businessUnitId, @Param(POSTED_BY) String postedBy,
                       @Param(CASE_REFERENCE) String caseRef, @Param(FUNCTION_CODE) String functionCode);

    void deleteByAssociatedRecordId(String defendantAccountId);

}
