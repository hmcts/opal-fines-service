package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnit;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnit_;

import java.util.Collection;
import java.util.Optional;

public class BusinessUnitLiteSpecs extends EntitySpecs<BusinessUnit.Lite> {

    public Specification<BusinessUnit.Lite> findBySearchCriteria(BusinessUnitSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericShort(criteria.getBusinessUnitId()).map(BusinessUnitLiteSpecs::equalsBusinessUnitId),
            notBlank(criteria.getBusinessUnitName()).map(BusinessUnitLiteSpecs::likeBusinessUnitName),
            notBlank(criteria.getBusinessUnitCode()).map(BusinessUnitLiteSpecs::likeBusinessUnitCode),
            notBlank(criteria.getBusinessUnitType()).map(BusinessUnitLiteSpecs::likeBusinessUnitType),
            notBlank(criteria.getAccountNumberPrefix()).map(BusinessUnitLiteSpecs::equalsAccountNumberPrefix),
            numericShort(criteria.getParentBusinessUnitId()).map(BusinessUnitLiteSpecs::equalsParentBusinessUnitId)
        ));
    }

    public Specification<BusinessUnit.Lite> referenceDataFilter(Optional<String> filter) {
        return Specification.allOf(specificationList(
            filter.filter(s -> !s.isBlank()).map(BusinessUnitLiteSpecs::likeAnyBusinessUnit)
        ));
    }

    public static Specification<BusinessUnit.Lite> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) -> equalsBusinessUnitIdPredicate(root, builder, businessUnitId);
    }

    public static Predicate equalsBusinessUnitIdPredicate(
        From<?, BusinessUnit.Lite> from, CriteriaBuilder builder, Short businessUnitId) {
        return builder.equal(from.get(BusinessUnit_.businessUnitId), businessUnitId);
    }

    public static Predicate equalsAnyBusinessUnitIdPredicate(
        From<?, BusinessUnit.Lite> from, CriteriaBuilder builder, Collection<Short> businessUnitId) {
        return from.get(BusinessUnit_.businessUnitId).in(businessUnitId);
    }

    public static Specification<BusinessUnit.Lite> likeBusinessUnitName(String businessUnitName) {
        return (root, query, builder) -> likeBusinessUnitNamePredicate(root, builder, businessUnitName);
    }

    public static Predicate likeBusinessUnitNamePredicate(
        From<?, BusinessUnit.Lite> from, CriteriaBuilder builder, String businessUnitName) {
        return likeWildcardPredicate(from.get(BusinessUnit_.businessUnitName), builder, businessUnitName);
    }

    public static Specification<BusinessUnit.Lite> likeBusinessUnitCode(String businessUnitCode) {
        return (root, query, builder) -> likeBusinessUnitCodePredicate(root, builder, businessUnitCode);
    }

    public static Predicate likeBusinessUnitCodePredicate(
        From<?, BusinessUnit.Lite> from, CriteriaBuilder builder, String businessUnitCode) {
        return likeWildcardPredicate(from.get(BusinessUnit_.businessUnitCode), builder, businessUnitCode);
    }

    public static Specification<BusinessUnit.Lite> likeBusinessUnitType(String businessUnitType) {
        return (root, query, builder) -> likeBusinessUnitTypePredicate(root, builder, businessUnitType);
    }

    public static Predicate likeBusinessUnitTypePredicate(
        From<?, BusinessUnit.Lite> from, CriteriaBuilder builder, String businessUnitType) {
        return likeWildcardPredicate(from.get(BusinessUnit_.businessUnitType), builder, businessUnitType);
    }

    public static Specification<BusinessUnit.Lite> equalsAccountNumberPrefix(String accountNumberPrefix) {
        return (root, query, builder) -> builder.equal(root.get(BusinessUnit_.accountNumberPrefix),
                                                       accountNumberPrefix);
    }

    public static Specification<BusinessUnit.Lite> equalsParentBusinessUnitId(Short parentBusinessUnitId) {
        return (root, query, builder) -> builder.equal(root.get(BusinessUnit_.parentBusinessUnitId),
                                                       parentBusinessUnitId);
    }

    public static Specification<BusinessUnit.Lite> likeAnyBusinessUnit(String filter) {
        return Specification.anyOf(
            likeBusinessUnitName(filter),
            likeBusinessUnitCode(filter),
            likeBusinessUnitType(filter)
        );
    }
}
