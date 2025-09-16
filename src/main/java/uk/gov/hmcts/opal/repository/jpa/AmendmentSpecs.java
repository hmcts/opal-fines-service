package uk.gov.hmcts.opal.repository.jpa;

import uk.gov.hmcts.opal.dto.search.AmendmentSearchDto;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity_;

import org.springframework.data.jpa.domain.Specification;

public class AmendmentSpecs extends EntitySpecs<AmendmentEntity> {

    public Specification<AmendmentEntity> findBySearchCriteria(AmendmentSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericLong(criteria.getAmendmentId()).map(AmendmentSpecs::equalsAmendmentId),
            numericShort(criteria.getBusinessUnitId()).map(AmendmentSpecs::equalsBusinessUnitId),
            notBlank(criteria.getAssociatedRecordType()).map(AmendmentSpecs::equalsAssociatedRecordType),
            notBlank(criteria.getAssociatedRecordId()).map(AmendmentSpecs::equalsAssociatedRecordId),
            notBlank(criteria.getAmendedBy()).map(AmendmentSpecs::equalsAmendedBy),
            numericShort(criteria.getFieldCode()).map(AmendmentSpecs::equalsFieldCode),
            notBlank(criteria.getOldValue()).map(AmendmentSpecs::likeOldValue),
            notBlank(criteria.getNewValue()).map(AmendmentSpecs::likeNewValue),
            notBlank(criteria.getCaseReference()).map(AmendmentSpecs::equalsCaseReference),
            notBlank(criteria.getFunctionCode()).map(AmendmentSpecs::equalsFunctionCode)
        ));
    }

    public static Specification<AmendmentEntity> equalsAmendmentId(Long amendmentId) {
        return (root, query, builder) -> builder.equal(root.get(AmendmentEntity_.amendmentId), amendmentId);
    }

    public static Specification<AmendmentEntity> equalsAssociatedRecordType(String associatedRecordType) {
        return (root, query, builder) -> builder.equal(
            root.get(AmendmentEntity_.associatedRecordType), associatedRecordType);
    }

    public static Specification<AmendmentEntity> equalsAssociatedRecordId(String associatedRecordId) {
        return (root, query, builder) -> builder.equal(
            root.get(AmendmentEntity_.associatedRecordId), associatedRecordId);
    }

    public static Specification<AmendmentEntity> equalsAmendedBy(String amendedBy) {
        return (root, query, builder) -> builder.equal(root.get(AmendmentEntity_.amendedBy), amendedBy);
    }

    public static Specification<AmendmentEntity> equalsFieldCode(Short fieldCode) {
        return (root, query, builder) -> builder.equal(root.get(AmendmentEntity_.fieldCode), fieldCode);
    }

    public static Specification<AmendmentEntity> likeOldValue(String oldValue) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(AmendmentEntity_.oldValue), builder, oldValue);
    }

    public static Specification<AmendmentEntity> likeNewValue(String newValue) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(AmendmentEntity_.newValue), builder, newValue);
    }

    public static Specification<AmendmentEntity> equalsCaseReference(String caseReference) {
        return (root, query, builder) -> builder.equal(root.get(AmendmentEntity_.caseReference), caseReference);
    }

    public static Specification<AmendmentEntity> equalsFunctionCode(String functionCode) {
        return (root, query, builder) -> builder.equal(root.get(AmendmentEntity_.functionCode), functionCode);
    }

    public static Specification<AmendmentEntity> equalsBusinessUnitId(Short businessUnit) {
        return (root, query, builder) -> builder.equal(root.get(AmendmentEntity_.businessUnitId), businessUnit);
    }
}
