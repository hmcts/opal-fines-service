package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.LogAuditDetailSearchDto;
import uk.gov.hmcts.opal.entity.LogActionEntity;
import uk.gov.hmcts.opal.entity.LogAuditDetailEntity;
import uk.gov.hmcts.opal.entity.LogAuditDetailEntity_;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnit;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitLiteSpecs.equalsBusinessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitLiteSpecs.likeBusinessUnitNamePredicate;
import static uk.gov.hmcts.opal.repository.jpa.LogActionSpecs.equalsLogActionIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.LogActionSpecs.likeLogActionNamePredicate;

public class LogAuditDetailSpecs extends EntitySpecs<LogAuditDetailEntity> {

    public Specification<LogAuditDetailEntity> findBySearchCriteria(LogAuditDetailSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getLogAuditDetailId()).map(LogAuditDetailSpecs::equalsLogAuditDetailId),
            notBlank(criteria.getUserId()).map(LogAuditDetailSpecs::equalsUserId),
            numericShort(criteria.getLogActionId()).map(LogAuditDetailSpecs::equalsLogActionId),
            notBlank(criteria.getLogActionName()).map(LogAuditDetailSpecs::likeLogActionName),
            notBlank(criteria.getAccountNumber()).map(LogAuditDetailSpecs::equalsAccountNumber),
            numericShort(criteria.getBusinessUnitId()).map(LogAuditDetailSpecs::equalsBusinessUnitId),
            notBlank(criteria.getBusinessUnitName()).map(LogAuditDetailSpecs::likeBusinessUnitName)
        ));
    }

    public static Specification<LogAuditDetailEntity> equalsLogAuditDetailId(String logAuditDetailId) {
        return (root, query, builder) -> builder.equal(root.get(LogAuditDetailEntity_.logAuditDetailId),
                                                       logAuditDetailId);
    }

    public static Specification<LogAuditDetailEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Specification<LogAuditDetailEntity> likeBusinessUnitName(String businessUnitName) {
        return (root, query, builder) ->
            likeBusinessUnitNamePredicate(joinBusinessUnit(root), builder, businessUnitName);
    }

    public static Specification<LogAuditDetailEntity> equalsLogActionId(Short logActionId) {
        return (root, query, builder) ->
            equalsLogActionIdPredicate(joinLogAction(root), builder, logActionId);
    }

    public static Specification<LogAuditDetailEntity> likeLogActionName(String logActionName) {
        return (root, query, builder) ->
            likeLogActionNamePredicate(joinLogAction(root), builder, logActionName);
    }

    public static Specification<LogAuditDetailEntity> equalsAccountNumber(String accountNumber) {
        return (root, query, builder) -> builder.equal(root.get(LogAuditDetailEntity_.accountNumber), accountNumber);
    }

    public static Specification<LogAuditDetailEntity> equalsUserId(String userId) {
        return (root, query, builder) -> builder.equal(root.get(LogAuditDetailEntity_.userId), userId);
    }

    public static Join<LogAuditDetailEntity, BusinessUnit.Lite> joinBusinessUnit(
        From<?, LogAuditDetailEntity> from) {
        return from.join(LogAuditDetailEntity_.businessUnit);
    }

    public static Join<LogAuditDetailEntity, LogActionEntity> joinLogAction(
        From<?, LogAuditDetailEntity> from) {
        return from.join(LogAuditDetailEntity_.logAction);
    }
}
