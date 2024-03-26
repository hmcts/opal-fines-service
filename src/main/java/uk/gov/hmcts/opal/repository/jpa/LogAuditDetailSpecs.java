package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.LogAuditDetailSearchDto;
import uk.gov.hmcts.opal.entity.LogAuditDetailEntity;
import uk.gov.hmcts.opal.entity.LogAuditDetailEntity_;

public class LogAuditDetailSpecs extends EntitySpecs<LogAuditDetailEntity> {

    public Specification<LogAuditDetailEntity> findBySearchCriteria(LogAuditDetailSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getLogAuditDetailId()).map(LogAuditDetailSpecs::equalsLogAuditDetailId),
            notBlank(criteria.getBusinessUnitId()).map(LogAuditDetailSpecs::equalsBusinessUnitId),
            notBlank(criteria.getLogActionId()).map(LogAuditDetailSpecs::equalsLogActionId),
            notBlank(criteria.getAccountNumber()).map(LogAuditDetailSpecs::equalsAccountNumber),
            notBlank(criteria.getUserId()).map(LogAuditDetailSpecs::equalsUserId)
        ));
    }

    public static Specification<LogAuditDetailEntity> equalsLogAuditDetailId(String logAuditDetailId) {
        return (root, query, builder) -> builder.equal(root.get(LogAuditDetailEntity_.logAuditDetailId),
                                                       logAuditDetailId);
    }

    public static Specification<LogAuditDetailEntity> equalsBusinessUnitId(String businessUnitId) {
        return (root, query, builder) -> builder.equal(root.get(LogAuditDetailEntity_.businessUnitId), businessUnitId);
    }

    public static Specification<LogAuditDetailEntity> equalsLogActionId(String logActionId) {
        return (root, query, builder) -> builder.equal(root.get(LogAuditDetailEntity_.logActionId), logActionId);
    }

    public static Specification<LogAuditDetailEntity> equalsAccountNumber(String accountNumber) {
        return (root, query, builder) -> builder.equal(root.get(LogAuditDetailEntity_.accountNumber), accountNumber);
    }

    public static Specification<LogAuditDetailEntity> equalsUserId(String userId) {
        return (root, query, builder) -> builder.equal(root.get(LogAuditDetailEntity_.userId), userId);
    }

}
