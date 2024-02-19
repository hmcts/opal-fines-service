package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity_;

public class BusinessUnitSpecs extends EntitySpecs<BusinessUnitEntity> {

    public Specification<BusinessUnitEntity> findBySearchCriteria(BusinessUnitSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getBusinessUnitId()).map(BusinessUnitSpecs::equalsBusinessUnitId),
            notBlank(criteria.getBusinessUnitName()).map(BusinessUnitSpecs::equalsBusinessUnitName),
            notBlank(criteria.getBusinessUnitCode()).map(BusinessUnitSpecs::likeBusinessUnitCode),
            notBlank(criteria.getBusinessUnitType()).map(BusinessUnitSpecs::likeBusinessUnitType),
            notBlank(criteria.getAccountNumberPrefix()).map(BusinessUnitSpecs::equalsAccountNumberPrefix),
            notBlank(criteria.getParentBusinessUnitId()).map(BusinessUnitSpecs::equalsParentBusinessUnitId)
        ));
    }

    public static Specification<BusinessUnitEntity> equalsBusinessUnitId(String businessUnitId) {
        return (root, query, builder) -> businessUnitIdPredicate(root, builder, businessUnitId);
    }

    public static Predicate businessUnitIdPredicate(
        From<?, BusinessUnitEntity> from, CriteriaBuilder builder, String businessUnitId) {
        return builder.equal(from.get(BusinessUnitEntity_.businessUnitId), businessUnitId);
    }

    public static Specification<BusinessUnitEntity> equalsBusinessUnitName(String businessUnitName) {
        return (root, query, builder) -> businessUnitNamePredicate(root, builder, businessUnitName);
    }

    public static Predicate businessUnitNamePredicate(
        From<?, BusinessUnitEntity> from, CriteriaBuilder builder, String businessUnitName) {
        return likeWildcardPredicate(from.get(BusinessUnitEntity_.businessUnitName), builder, businessUnitName);
    }

    public static Specification<BusinessUnitEntity> likeBusinessUnitCode(String businessUnitCode) {
        return (root, query, builder) -> businessUnitCodePredicate(root, builder, businessUnitCode);
    }

    public static Predicate businessUnitCodePredicate(
        From<?, BusinessUnitEntity> from, CriteriaBuilder builder, String businessUnitCode) {
        return likeWildcardPredicate(from.get(BusinessUnitEntity_.businessUnitCode), builder, businessUnitCode);
    }

    public static Specification<BusinessUnitEntity> likeBusinessUnitType(String businessUnitType) {
        return (root, query, builder) -> businessUnitTypePredicate(root, builder, businessUnitType);
    }

    public static Predicate businessUnitTypePredicate(
        From<?, BusinessUnitEntity> from, CriteriaBuilder builder, String businessUnitType) {
        return likeWildcardPredicate(from.get(BusinessUnitEntity_.businessUnitType), builder, businessUnitType);
    }

    public static Specification<BusinessUnitEntity> equalsAccountNumberPrefix(String accountNumberPrefix) {
        return (root, query, builder) -> builder.equal(root.get(BusinessUnitEntity_.accountNumberPrefix),
                                                       accountNumberPrefix);
    }

    public static Specification<BusinessUnitEntity> equalsParentBusinessUnitId(String parentBusinessUnitId) {
        return (root, query, builder) -> parentBusinessUnitIdPredicate(root, builder, parentBusinessUnitId);
    }

    public static Predicate parentBusinessUnitIdPredicate(
        From<?, BusinessUnitEntity> from, CriteriaBuilder builder, String parentBusinessUnitId) {
        return builder.equal(from.get(BusinessUnitEntity_.parentBusinessUnitId), parentBusinessUnitId);
    }

}
