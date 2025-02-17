package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.AmendmentSearchDto;
import uk.gov.hmcts.opal.entity.AmendmentEntity;
import uk.gov.hmcts.opal.entity.AmendmentEntity_;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnit;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitLiteSpecs.equalsBusinessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitLiteSpecs.likeBusinessUnitNamePredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitLiteSpecs.likeBusinessUnitTypePredicate;

public class AmendmentSpecs extends EntitySpecs<AmendmentEntity> {

    public Specification<AmendmentEntity> findBySearchCriteria(AmendmentSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericLong(criteria.getAmendmentId()).map(AmendmentSpecs::equalsAmendmentId),
            numericShort(criteria.getBusinessUnitId()).map(AmendmentSpecs::equalsBusinessUnitId),
            notBlank(criteria.getBusinessUnitName()).map(AmendmentSpecs::likeBusinessUnitName),
            notBlank(criteria.getBusinessUnitType()).map(AmendmentSpecs::likeBusinessUnitType),
            // numericShort(criteria.getParentBusinessUnitId()).map(AmendmentSpecs::equalsParentBusinessUnitId),
            notBlank(criteria.getAssociatedRecordType()).map(AmendmentSpecs::likeAssociatedRecordType),
            notBlank(criteria.getAssociatedRecordId()).map(AmendmentSpecs::likeAssociatedRecordId),
            notBlank(criteria.getAmendedBy()).map(AmendmentSpecs::likeAmendedBy),
            numericShort(criteria.getFieldCode()).map(AmendmentSpecs::equalsFieldCode),
            notBlank(criteria.getOldValue()).map(AmendmentSpecs::likeOldValue),
            notBlank(criteria.getNewValue()).map(AmendmentSpecs::likeNewValue),
            notBlank(criteria.getCaseReference()).map(AmendmentSpecs::likeCaseReference),
            notBlank(criteria.getFunctionCode()).map(AmendmentSpecs::likeFunctionCode)
        ));
    }

    public static Specification<AmendmentEntity> equalsAmendmentId(Long amendmentId) {
        return (root, query, builder) -> builder.equal(root.get(AmendmentEntity_.amendmentId), amendmentId);
    }

    public static Specification<AmendmentEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Specification<AmendmentEntity> likeBusinessUnitName(String businessUnitName) {
        return (root, query, builder) ->
            likeBusinessUnitNamePredicate(joinBusinessUnit(root), builder, businessUnitName);
    }

    public static Specification<AmendmentEntity> likeBusinessUnitType(String businessUnitType) {
        return (root, query, builder) ->
            likeBusinessUnitTypePredicate(joinBusinessUnit(root), builder, businessUnitType);
    }

    // public static Specification<AmendmentEntity> equalsParentBusinessUnitId(Short parentBusinessUnitId) {
    //     return (root, query, builder) ->
    //         equalsParentBusinessUnitIdPredicate(joinBusinessUnit(root), builder, parentBusinessUnitId);
    // }

    public static Specification<AmendmentEntity> likeAssociatedRecordType(String associatedRecordType) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(AmendmentEntity_.associatedRecordType), builder, associatedRecordType);
    }

    public static Specification<AmendmentEntity> likeAssociatedRecordId(String associatedRecordId) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(AmendmentEntity_.associatedRecordId), builder, associatedRecordId);
    }

    public static Specification<AmendmentEntity> likeAmendedBy(String amendedBy) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(AmendmentEntity_.amendedBy), builder, amendedBy);
    }

    public static Specification<AmendmentEntity> equalsFieldCode(Short fieldCode) {
        return (root, query, builder) -> builder.equal(root.get(AmendmentEntity_.fieldCode), fieldCode);
    }

    public static Specification<AmendmentEntity> likeOldValue(String oldValue) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(AmendmentEntity_.oldValue), builder, oldValue);
    }

    public static Specification<AmendmentEntity> likeNewValue(String newValue) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(AmendmentEntity_.newValue), builder, newValue);
    }

    public static Specification<AmendmentEntity> likeCaseReference(String caseReference) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(AmendmentEntity_.caseReference), builder, caseReference);
    }

    public static Specification<AmendmentEntity> likeFunctionCode(String functionCode) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(AmendmentEntity_.functionCode), builder, functionCode);
    }


    public static Join<AmendmentEntity, BusinessUnit.Lite> joinBusinessUnit(
        From<?, AmendmentEntity> from) {
        return from.join(AmendmentEntity_.businessUnit);
    }

}
