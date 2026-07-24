package uk.gov.hmcts.opal.repository;

import static uk.gov.hmcts.opal.entity.interfacejob.InterfaceJobStoredProcedureNames.INTERFACE_JOB_ID;
import static uk.gov.hmcts.opal.entity.interfacejob.InterfaceJobStoredProcedureNames.BUSINESS_UNIT_ID;
import static uk.gov.hmcts.opal.entity.interfacejob.InterfaceJobStoredProcedureNames.JPA_PROC_NAME;
import static uk.gov.hmcts.opal.entity.interfacejob.InterfaceJobStoredProcedureNames.POSTED_BY;
import static uk.gov.hmcts.opal.entity.interfacejob.InterfaceJobStoredProcedureNames.POSTED_BY_NAME;

import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;

@Repository
public interface InterfaceJobRepository extends JpaRepository<InterfaceJobEntity, Long>,
    JpaSpecificationExecutor<InterfaceJobEntity> {

    @Override
    @EntityGraph(attributePaths = {"businessUnit", "interfaceFiles"})
    <S extends InterfaceJobEntity, R> R findBy(
        Specification<InterfaceJobEntity> specification,
        Function<? super JpaSpecificationExecutor.SpecificationFluentQuery<S>, R> queryFunction);

    @Procedure(name = JPA_PROC_NAME)
    Long processPaymentsInJob(@Param(INTERFACE_JOB_ID) Long interfaceJobId,
                              @Param(BUSINESS_UNIT_ID) Short businessUnitId,
                              @Param(POSTED_BY) String postedBy,
                              @Param(POSTED_BY_NAME) String postedByName);

}
