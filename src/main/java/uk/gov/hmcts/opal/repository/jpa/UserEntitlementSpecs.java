package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.UserEntitlementSearchDto;
import uk.gov.hmcts.opal.entity.ApplicationFunctionEntity;
import uk.gov.hmcts.opal.entity.BusinessUnitUserEntity;
import uk.gov.hmcts.opal.entity.UserEntitlementEntity;
import uk.gov.hmcts.opal.entity.UserEntitlementEntity_;

import static uk.gov.hmcts.opal.repository.jpa.ApplicationFunctionSpecs.likeFunctionNamePredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.likeBusinessUnitNamePredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.likeBusinessUnitTypePredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsParentBusinessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitUserSpecs.equalsBusinessUnitUserIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitUserSpecs.joinBusinessUnit;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitUserSpecs.joinUser;
import static uk.gov.hmcts.opal.repository.jpa.UserSpecs.likeUserDescriptionPredicate;
import static uk.gov.hmcts.opal.repository.jpa.UserSpecs.equalsUserIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.UserSpecs.likeUsernamePredicate;

public class UserEntitlementSpecs extends EntitySpecs<UserEntitlementEntity> {

    public Specification<UserEntitlementEntity> findBySearchCriteria(UserEntitlementSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericLong(criteria.getUserEntitlementId()).map(UserEntitlementSpecs::equalsUserEntitlementId),
            notBlank(criteria.getBusinessUnitUserId()).map(UserEntitlementSpecs::likeBusinessUnitUserId),
            numericShort(criteria.getBusinessUnitId()).map(UserEntitlementSpecs::equalsBusinessUnitId),
            notBlank(criteria.getBusinessUnitName()).map(UserEntitlementSpecs::likeBusinessUnitName),
            notBlank(criteria.getBusinessUnitType()).map(UserEntitlementSpecs::likeBusinessUnitType),
            notBlank(criteria.getParentBusinessUnitId()).map(UserEntitlementSpecs::equalsParentBusinessUnitId),
            numericLong(criteria.getUserId()).map(UserEntitlementSpecs::likeUserId),
            notBlank(criteria.getUsername()).map(UserEntitlementSpecs::likeUsername),
            notBlank(criteria.getUserDescription()).map(UserEntitlementSpecs::likeUserDescription),
            notBlank(criteria.getFunctionName()).map(UserEntitlementSpecs::likeFunctionName)
        ));
    }

    public static Specification<UserEntitlementEntity> equalsUserEntitlementId(Long userEntitlementId) {
        return (root, query, builder) -> builder.equal(root.get(UserEntitlementEntity_.userEntitlementId),
                                                       userEntitlementId);
    }

    public static Specification<UserEntitlementEntity> likeBusinessUnitUserId(String businessUnitUserId) {
        return (root, query, builder) ->
            equalsBusinessUnitUserIdPredicate(joinBusinessUnitUser(root), builder, businessUnitUserId);
    }

    public static Specification<UserEntitlementEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(joinBusinessUnitUser(root)), builder, businessUnitId);
    }

    public static Specification<UserEntitlementEntity> likeBusinessUnitName(String businessUnitName) {
        return (root, query, builder) ->
            likeBusinessUnitNamePredicate(joinBusinessUnit(joinBusinessUnitUser(root)), builder, businessUnitName);
    }

    public static Specification<UserEntitlementEntity> likeBusinessUnitType(String businessUnitType) {
        return (root, query, builder) ->
            likeBusinessUnitTypePredicate(joinBusinessUnit(joinBusinessUnitUser(root)), builder, businessUnitType);
    }

    public static Specification<UserEntitlementEntity> equalsParentBusinessUnitId(String parentBusinessUnitId) {
        return (root, query, builder) ->
            equalsParentBusinessUnitIdPredicate(joinBusinessUnit(joinBusinessUnitUser(root)), builder,
                                                parentBusinessUnitId);
    }

    public static Specification<UserEntitlementEntity> likeUserId(Long userId) {
        return (root, query, builder) -> equalsUserIdPredicate(joinUser(joinBusinessUnitUser(root)), builder, userId);
    }

    public static Specification<UserEntitlementEntity> likeUsername(String username) {
        return (root, query, builder) -> likeUsernamePredicate(joinUser(joinBusinessUnitUser(root)), builder, username);
    }

    public static Specification<UserEntitlementEntity> equalsUsername(String username) {
        return (root, query, builder) -> likeUsernamePredicate(
            joinUser(joinBusinessUnitUser(root)), builder, username);
    }

    public static Specification<UserEntitlementEntity> likeUserDescription(String description) {
        return (root, query, builder) -> likeUserDescriptionPredicate(joinUser(joinBusinessUnitUser(root)), builder,
                                                                      description);
    }

    public static Specification<UserEntitlementEntity> likeFunctionName(String functionName) {
        return (root, query, builder) -> likeFunctionNamePredicate(joinApplicationFunction(root), builder,
                                                                   functionName);
    }

    public static Join<UserEntitlementEntity, BusinessUnitUserEntity> joinBusinessUnitUser(
        Root<UserEntitlementEntity> root) {
        return root.join(UserEntitlementEntity_.businessUnitUser);
    }

    public static Join<UserEntitlementEntity, ApplicationFunctionEntity> joinApplicationFunction(
        Root<UserEntitlementEntity> root) {
        return root.join(UserEntitlementEntity_.applicationFunction);
    }
}
