package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.StoredProcedureNames;

import java.util.Map;

@Repository
public interface DraftAccountRepository extends JpaRepository<DraftAccountEntity, Long>,
    JpaSpecificationExecutor<DraftAccountEntity>, StoredProcedureNames {

    @Procedure(name = JPA_PROC_NAME)
    Map<String, Object> createDefendantAccount(
        @Param(DRAFT_ACC_ID) Long draftAccountId, @Param(BUSINESS_UNIT_ID) Short businessUnitId,
        @Param(POSTED_BY) String postedBy, @Param(POSTED_BY_NAME) String postedByName);

    @Procedure(procedureName = DB_PROC_NAME, outputParameterName = DEF_ACC_ID)
    Long exampleSimplerStoredProcedureCall_SingleOutParam(
        @Param(DRAFT_ACC_ID) Long draftAccountId, @Param(BUSINESS_UNIT_ID) Short businessUnitId,
        @Param(POSTED_BY) String postedBy, @Param(POSTED_BY_NAME) String postedByName);
}
