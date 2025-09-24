package uk.gov.hmcts.opal.repository.jpa;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity_;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class BusinessUnitSpecs extends EntitySpecs<BusinessUnitFullEntity> {

    public Specification<BusinessUnitFullEntity> findBySearchCriteria(BusinessUnitSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericShort(criteria.getBusinessUnitId()).map(BusinessUnitSpecs::equalsBusinessUnitId),
            notBlank(criteria.getBusinessUnitName()).map(BusinessUnitSpecs::likeBusinessUnitName),
            notBlank(criteria.getBusinessUnitCode()).map(BusinessUnitSpecs::likeBusinessUnitCode),
            notBlank(criteria.getBusinessUnitType()).map(BusinessUnitSpecs::likeBusinessUnitType),
            notBlank(criteria.getAccountNumberPrefix()).map(BusinessUnitSpecs::equalsAccountNumberPrefix),
            numericShort(criteria.getParentBusinessUnitId()).map(BusinessUnitSpecs::equalsParentBusinessUnitId)
        ));
    }

    public Specification<BusinessUnitFullEntity> referenceDataFilter(Optional<String> filter) {
        return Specification.allOf(specificationList(
            filter.filter(s -> !s.isBlank()).map(BusinessUnitSpecs::likeAnyBusinessUnit)
        ));
    }

    public static Specification<BusinessUnitFullEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) -> equalsBusinessUnitIdPredicate(root, builder, businessUnitId);
    }

    public static Specification<BusinessUnitFullEntity> businessUnitIdIsOneOf(List<Short> businessUnitIds) {
        return (root, query, builder) ->
            CollectionUtils.isEmpty(businessUnitIds)
                ? builder.disjunction() // No business unit IDs means no filter
                : equalsAnyBusinessUnitIdPredicate(root, builder, businessUnitIds);
    }

    public static Predicate equalsBusinessUnitIdPredicate(
            From<?, BusinessUnitFullEntity> from, CriteriaBuilder builder, Short businessUnitId) {
        return builder.equal(from.get(BusinessUnitFullEntity_.businessUnitId), businessUnitId);
    }

    public static Predicate equalsAnyBusinessUnitIdPredicate(
            From<?, BusinessUnitFullEntity> from, CriteriaBuilder builder, Collection<Short> businessUnitId) {
        return from.get(BusinessUnitFullEntity_.businessUnitId).in(businessUnitId);
    }

    public static Specification<BusinessUnitFullEntity> likeBusinessUnitName(String businessUnitName) {
        return (root, query, builder) -> likeBusinessUnitNamePredicate(root, builder, businessUnitName);
    }

    public static Predicate likeBusinessUnitNamePredicate(
            From<?, BusinessUnitFullEntity> from, CriteriaBuilder builder, String businessUnitName) {
        return likeWildcardPredicate(from.get(BusinessUnitFullEntity_.businessUnitName), builder, businessUnitName);
    }

    public static Specification<BusinessUnitFullEntity> likeBusinessUnitCode(String businessUnitCode) {
        return (root, query, builder) -> likeBusinessUnitCodePredicate(root, builder, businessUnitCode);
    }

    public static Predicate likeBusinessUnitCodePredicate(
            From<?, BusinessUnitFullEntity> from, CriteriaBuilder builder, String businessUnitCode) {
        return likeWildcardPredicate(from.get(BusinessUnitFullEntity_.businessUnitCode), builder, businessUnitCode);
    }

    public static Specification<BusinessUnitFullEntity> likeBusinessUnitType(String businessUnitType) {
        return (root, query, builder) -> likeBusinessUnitTypePredicate(root, builder, businessUnitType);
    }

    public static Predicate likeBusinessUnitTypePredicate(
            From<?, BusinessUnitFullEntity> from, CriteriaBuilder builder, String businessUnitType) {
        return likeWildcardPredicate(from.get(BusinessUnitFullEntity_.businessUnitType), builder, businessUnitType);
    }

    public static Specification<BusinessUnitFullEntity> equalsAccountNumberPrefix(String accountNumberPrefix) {
        return (root, query, builder) -> builder.equal(root.get(BusinessUnitFullEntity_.accountNumberPrefix),
            accountNumberPrefix);
    }

    public static Specification<BusinessUnitFullEntity> equalsParentBusinessUnitId(Short parentBusinessUnitId) {
        return (root, query, builder) -> equalsParentBusinessUnitIdPredicate(root, builder, parentBusinessUnitId);
    }

    public static Predicate equalsParentBusinessUnitIdPredicate(
            From<?, BusinessUnitFullEntity> from, CriteriaBuilder builder, Short parentBusinessUnitId) {
        return equalsBusinessUnitIdPredicate(joinParentBusinessUnit(from), builder, parentBusinessUnitId);
    }


    public static Specification<BusinessUnitFullEntity> likeAnyBusinessUnit(String filter) {
        return Specification.anyOf(
            likeBusinessUnitName(filter),
            likeBusinessUnitCode(filter),
            likeBusinessUnitType(filter)
        );
    }

    public static Join<BusinessUnitFullEntity, BusinessUnitFullEntity> joinParentBusinessUnit(
        From<?, BusinessUnitFullEntity> from) {
        return from.join(BusinessUnitFullEntity_.parentBusinessUnit);
    }
}
