package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitCore;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitCore_;

import java.util.Collection;
import java.util.Optional;

public class BusinessUnitCoreSpecs extends EntitySpecs<BusinessUnitCore> {

    public Specification<BusinessUnitCore> findBySearchCriteria(BusinessUnitSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericShort(criteria.getBusinessUnitId()).map(BusinessUnitCoreSpecs::equalsBusinessUnitId),
            notBlank(criteria.getBusinessUnitName()).map(BusinessUnitCoreSpecs::likeBusinessUnitName),
            notBlank(criteria.getBusinessUnitCode()).map(BusinessUnitCoreSpecs::likeBusinessUnitCode),
            notBlank(criteria.getBusinessUnitType()).map(BusinessUnitCoreSpecs::likeBusinessUnitType),
            notBlank(criteria.getAccountNumberPrefix()).map(BusinessUnitCoreSpecs::equalsAccountNumberPrefix),
            numericShort(criteria.getParentBusinessUnitId()).map(BusinessUnitCoreSpecs::equalsParentBusinessUnitId)
        ));
    }

    public Specification<BusinessUnitCore> referenceDataFilter(Optional<String> filter) {
        return Specification.allOf(specificationList(
            filter.filter(s -> !s.isBlank()).map(BusinessUnitCoreSpecs::likeAnyBusinessUnit)
        ));
    }

    public static Specification<BusinessUnitCore> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) -> equalsBusinessUnitIdPredicate(root, builder, businessUnitId);
    }

    public static Predicate equalsBusinessUnitIdPredicate(
        From<?, BusinessUnitCore> from, CriteriaBuilder builder, Short businessUnitId) {
        return builder.equal(from.get(BusinessUnitCore_.businessUnitId), businessUnitId);
    }

    public static Predicate equalsAnyBusinessUnitIdPredicate(
        From<?, BusinessUnitCore> from, CriteriaBuilder builder, Collection<Short> businessUnitId) {
        return from.get(BusinessUnitCore_.businessUnitId).in(businessUnitId);
    }

    public static Specification<BusinessUnitCore> likeBusinessUnitName(String businessUnitName) {
        return (root, query, builder) -> likeBusinessUnitNamePredicate(root, builder, businessUnitName);
    }

    public static Predicate likeBusinessUnitNamePredicate(
        From<?, BusinessUnitCore> from, CriteriaBuilder builder, String businessUnitName) {
        return likeWildcardPredicate(from.get(BusinessUnitCore_.businessUnitName), builder, businessUnitName);
    }

    public static Specification<BusinessUnitCore> likeBusinessUnitCode(String businessUnitCode) {
        return (root, query, builder) -> likeBusinessUnitCodePredicate(root, builder, businessUnitCode);
    }

    public static Predicate likeBusinessUnitCodePredicate(
        From<?, BusinessUnitCore> from, CriteriaBuilder builder, String businessUnitCode) {
        return likeWildcardPredicate(from.get(BusinessUnitCore_.businessUnitCode), builder, businessUnitCode);
    }

    public static Specification<BusinessUnitCore> likeBusinessUnitType(String businessUnitType) {
        return (root, query, builder) -> likeBusinessUnitTypePredicate(root, builder, businessUnitType);
    }

    public static Predicate likeBusinessUnitTypePredicate(
        From<?, BusinessUnitCore> from, CriteriaBuilder builder, String businessUnitType) {
        return likeWildcardPredicate(from.get(BusinessUnitCore_.businessUnitType), builder, businessUnitType);
    }

    public static Specification<BusinessUnitCore> equalsAccountNumberPrefix(String accountNumberPrefix) {
        return (root, query, builder) -> builder.equal(root.get(BusinessUnitCore_.accountNumberPrefix),
                                                       accountNumberPrefix);
    }

    public static Specification<BusinessUnitCore> equalsParentBusinessUnitId(Short parentBusinessUnitId) {
        // return (root, query, builder) -> equalsParentBusinessUnitIdPredicate(root, builder, parentBusinessUnitId);
        return (root, query, builder) -> builder.equal(root.get(BusinessUnitCore_.parentBusinessUnitId),
                                                       parentBusinessUnitId);
    }

    public static Specification<BusinessUnitCore> likeAnyBusinessUnit(String filter) {
        return Specification.anyOf(
            likeBusinessUnitName(filter),
            likeBusinessUnitCode(filter),
            likeBusinessUnitType(filter)
        );
    }
}
