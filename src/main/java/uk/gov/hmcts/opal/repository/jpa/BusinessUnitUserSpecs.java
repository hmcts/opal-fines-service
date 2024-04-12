package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.BusinessUnitUserSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.BusinessUnitUserEntity;
import uk.gov.hmcts.opal.entity.BusinessUnitUserEntity_;
import uk.gov.hmcts.opal.entity.UserEntity;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.likeBusinessUnitNamePredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.likeBusinessUnitTypePredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsParentBusinessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.UserSpecs.likeUserDescriptionPredicate;
import static uk.gov.hmcts.opal.repository.jpa.UserSpecs.equalsUserIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.UserSpecs.likeUsernamePredicate;

public class BusinessUnitUserSpecs extends EntitySpecs<BusinessUnitUserEntity> {

    public Specification<BusinessUnitUserEntity> findBySearchCriteria(BusinessUnitUserSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getBusinessUnitUserId()).map(BusinessUnitUserSpecs::equalsBusinessUnitUserId),
            numericShort(criteria.getBusinessUnitId()).map(BusinessUnitUserSpecs::equalsBusinessUnitId),
            notBlank(criteria.getBusinessUnitName()).map(BusinessUnitUserSpecs::likeBusinessUnitName),
            notBlank(criteria.getBusinessUnitType()).map(BusinessUnitUserSpecs::likeBusinessUnitType),
            numeric(criteria.getParentBusinessUnitId()).map(BusinessUnitUserSpecs::equalsParentBusinessUnitId),
            numericLong(criteria.getUserId()).map(BusinessUnitUserSpecs::likeUserId),
            notBlank(criteria.getUsername()).map(BusinessUnitUserSpecs::likeUsername),
            notBlank(criteria.getUserDescription()).map(BusinessUnitUserSpecs::likeUserDescription)
        ));
    }

    public static Specification<BusinessUnitUserEntity> equalsBusinessUnitUserId(String businessUnitUserId) {
        return (root, query, builder) -> equalsBusinessUnitUserIdPredicate(root, builder, businessUnitUserId);
    }

    public static Predicate equalsBusinessUnitUserIdPredicate(From<?, BusinessUnitUserEntity> from,
                                                              CriteriaBuilder builder, String businessUnitUserId) {
        return likeWildcardPredicate(from.get(BusinessUnitUserEntity_.businessUnitUserId), builder, businessUnitUserId);
    }

    public static Specification<BusinessUnitUserEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Specification<BusinessUnitUserEntity> likeBusinessUnitName(String businessUnitName) {
        return (root, query, builder) ->
            likeBusinessUnitNamePredicate(joinBusinessUnit(root), builder, businessUnitName);
    }

    public static Specification<BusinessUnitUserEntity> likeBusinessUnitType(String businessUnitType) {
        return (root, query, builder) ->
            likeBusinessUnitTypePredicate(joinBusinessUnit(root), builder, businessUnitType);
    }

    public static Specification<BusinessUnitUserEntity> equalsParentBusinessUnitId(String parentBusinessUnitId) {
        return (root, query, builder) ->
            equalsParentBusinessUnitIdPredicate(joinBusinessUnit(root), builder, parentBusinessUnitId);
    }

    public static Specification<BusinessUnitUserEntity> likeUserId(Long userId) {
        return (root, query, builder) -> equalsUserIdPredicate(joinUser(root), builder, userId);
    }

    public static Specification<BusinessUnitUserEntity> likeUsername(String username) {
        return (root, query, builder) -> likeUsernamePredicate(joinUser(root), builder, username);
    }

    public static Specification<BusinessUnitUserEntity> likeUserDescription(String description) {
        return (root, query, builder) -> likeUserDescriptionPredicate(joinUser(root), builder, description);
    }

    public static Join<BusinessUnitUserEntity, BusinessUnitEntity> joinBusinessUnit(
        From<?, BusinessUnitUserEntity> from) {
        return from.join(BusinessUnitUserEntity_.businessUnit);
    }

    public static Join<BusinessUnitUserEntity, UserEntity> joinUser(From<?, BusinessUnitUserEntity> from) {
        return from.join(BusinessUnitUserEntity_.user);
    }
}
