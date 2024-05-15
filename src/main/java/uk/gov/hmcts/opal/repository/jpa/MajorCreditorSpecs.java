package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.MajorCreditorSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.MajorCreditorEntity;
import uk.gov.hmcts.opal.entity.MajorCreditorEntity_;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.likeBusinessUnitNamePredicate;

public class MajorCreditorSpecs extends AddressSpecs<MajorCreditorEntity> {

    public Specification<MajorCreditorEntity> findBySearchCriteria(MajorCreditorSearchDto criteria) {
        return Specification.allOf(specificationList(
            findByAddressCriteria(criteria),
            numericLong(criteria.getMajorCreditorId()).map(MajorCreditorSpecs::equalsMajorCreditorId),
            numericShort(criteria.getBusinessUnitId()).map(MajorCreditorSpecs::equalsBusinessUnitId),
            notBlank(criteria.getBusinessUnitName()).map(MajorCreditorSpecs::likeBusinessUnitName),
            numericShort(criteria.getMajorCreditorCode()).map(MajorCreditorSpecs::equalsMajorCreditorCode)
        ));
    }

    public static Specification<MajorCreditorEntity> equalsMajorCreditorId(Long majorCreditorId) {
        return (root, query, builder) -> equalsMajorCreditorIdPredicate(root, builder, majorCreditorId);
    }

    public static Predicate equalsMajorCreditorIdPredicate(
        From<?, MajorCreditorEntity> from, CriteriaBuilder builder, Long majorCreditorId) {
        return builder.equal(from.get(MajorCreditorEntity_.majorCreditorId), majorCreditorId);
    }

    public static Specification<MajorCreditorEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Specification<MajorCreditorEntity> likeBusinessUnitName(String businessUnitName) {
        return (root, query, builder) ->
            likeBusinessUnitNamePredicate(joinBusinessUnit(root), builder, businessUnitName);
    }

    public static Specification<MajorCreditorEntity> equalsMajorCreditorCode(Short majorCreditorCode) {
        return (root, query, builder) -> equalsMajorCreditorCodePredicate(root, builder, majorCreditorCode);
    }

    public static Predicate equalsMajorCreditorCodePredicate(
        From<?, MajorCreditorEntity> from, CriteriaBuilder builder, Short businessUnitId) {
        return builder.equal(from.get(MajorCreditorEntity_.majorCreditorCode), businessUnitId);
    }

    public static Join<MajorCreditorEntity, BusinessUnitEntity> joinBusinessUnit(From<?, MajorCreditorEntity> from) {
        return from.join(MajorCreditorEntity_.businessUnit);
    }

}
