package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity_;

import java.util.Collection;
import java.util.Optional;

public class BusinessUnitSpecs extends EntitySpecs<BusinessUnitEntity> {

    public Specification<BusinessUnitEntity> findBySearchCriteria(BusinessUnitSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericShort(criteria.getBusinessUnitId()).map(BusinessUnitSpecs::equalsBusinessUnitId),
            notBlank(criteria.getBusinessUnitName()).map(BusinessUnitSpecs::likeBusinessUnitName),
            notBlank(criteria.getBusinessUnitCode()).map(BusinessUnitSpecs::likeBusinessUnitCode),
            notBlank(criteria.getBusinessUnitType()).map(BusinessUnitSpecs::likeBusinessUnitType),
            notBlank(criteria.getAccountNumberPrefix()).map(BusinessUnitSpecs::equalsAccountNumberPrefix),
            numericShort(criteria.getParentBusinessUnitId()).map(BusinessUnitSpecs::equalsParentBusinessUnitId)
        ));
    }

    public Specification<BusinessUnitEntity> referenceDataFilter(Optional<String> filter) {
        return Specification.allOf(specificationList(
            filter.filter(s -> !s.isBlank()).map(BusinessUnitSpecs::likeAnyBusinessUnit)
        ));
    }

    public static Specification<BusinessUnitEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) -> equalsBusinessUnitIdPredicate(root, builder, businessUnitId);
    }

    public static Predicate equalsBusinessUnitIdPredicate(
        From<?, BusinessUnitEntity> from, CriteriaBuilder builder, Short businessUnitId) {
        return builder.equal(from.get(BusinessUnitEntity_.businessUnitId), businessUnitId);
    }

    public static Predicate equalsAnyBusinessUnitIdPredicate(
        From<?, BusinessUnitEntity> from, CriteriaBuilder builder, Collection<Short> businessUnitId) {
        return from.get(BusinessUnitEntity_.businessUnitId).in(businessUnitId);
    }

    public static Specification<BusinessUnitEntity> likeBusinessUnitName(String businessUnitName) {
        return (root, query, builder) -> likeBusinessUnitNamePredicate(root, builder, businessUnitName);
    }

    public static Predicate likeBusinessUnitNamePredicate(
        From<?, BusinessUnitEntity> from, CriteriaBuilder builder, String businessUnitName) {
        return likeWildcardPredicate(from.get(BusinessUnitEntity_.businessUnitName), builder, businessUnitName);
    }

    public static Specification<BusinessUnitEntity> likeBusinessUnitCode(String businessUnitCode) {
        return (root, query, builder) -> likeBusinessUnitCodePredicate(root, builder, businessUnitCode);
    }

    public static Predicate likeBusinessUnitCodePredicate(
        From<?, BusinessUnitEntity> from, CriteriaBuilder builder, String businessUnitCode) {
        return likeWildcardPredicate(from.get(BusinessUnitEntity_.businessUnitCode), builder, businessUnitCode);
    }

    public static Specification<BusinessUnitEntity> likeBusinessUnitType(String businessUnitType) {
        return (root, query, builder) -> likeBusinessUnitTypePredicate(root, builder, businessUnitType);
    }

    public static Predicate likeBusinessUnitTypePredicate(
        From<?, BusinessUnitEntity> from, CriteriaBuilder builder, String businessUnitType) {
        return likeWildcardPredicate(from.get(BusinessUnitEntity_.businessUnitType), builder, businessUnitType);
    }

    public static Specification<BusinessUnitEntity> equalsAccountNumberPrefix(String accountNumberPrefix) {
        return (root, query, builder) -> builder.equal(root.get(BusinessUnitEntity_.accountNumberPrefix),
                                                       accountNumberPrefix);
    }

    public static Specification<BusinessUnitEntity> equalsParentBusinessUnitId(Short parentBusinessUnitId) {
        return (root, query, builder) -> equalsParentBusinessUnitIdPredicate(root, builder, parentBusinessUnitId);
    }

    public static Predicate equalsParentBusinessUnitIdPredicate(
        From<?, BusinessUnitEntity> from, CriteriaBuilder builder, Short parentBusinessUnitId) {
        return equalsBusinessUnitIdPredicate(joinParentBusinessUnit(from), builder, parentBusinessUnitId);
    }


    public static Specification<BusinessUnitEntity> likeAnyBusinessUnit(String filter) {
        return Specification.anyOf(
            likeBusinessUnitName(filter),
            likeBusinessUnitCode(filter),
            likeBusinessUnitType(filter)
        );
    }

    public static Join<BusinessUnitEntity, BusinessUnitEntity> joinParentBusinessUnit(
        From<?, BusinessUnitEntity> from) {
        return from.join(BusinessUnitEntity_.parentBusinessUnit);
    }
}
