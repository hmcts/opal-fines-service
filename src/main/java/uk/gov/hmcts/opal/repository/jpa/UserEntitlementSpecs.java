package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.UserEntitlementSearchDto;
import uk.gov.hmcts.opal.entity.ApplicationFunctionEntity;
import uk.gov.hmcts.opal.entity.BusinessUnitUserEntity;
import uk.gov.hmcts.opal.entity.UserEntitlementEntity;
import uk.gov.hmcts.opal.entity.UserEntitlementEntity_;

import static uk.gov.hmcts.opal.repository.jpa.ApplicationFunctionSpecs.functionNamePredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.businessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.businessUnitNamePredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.businessUnitTypePredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.parentBusinessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitUserSpecs.businessUnitUserIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitUserSpecs.joinBusinessUnit;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitUserSpecs.joinUser;
import static uk.gov.hmcts.opal.repository.jpa.UserSpecs.equalsUsernamePredicate;
import static uk.gov.hmcts.opal.repository.jpa.UserSpecs.userDescriptionPredicate;
import static uk.gov.hmcts.opal.repository.jpa.UserSpecs.userIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.UserSpecs.usernamePredicate;

public class UserEntitlementSpecs extends EntitySpecs<UserEntitlementEntity> {

    public Specification<UserEntitlementEntity> findBySearchCriteria(UserEntitlementSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericLong(criteria.getUserEntitlementId()).map(UserEntitlementSpecs::equalsUserEntitlementId),
            notBlank(criteria.getBusinessUnitUserId()).map(UserEntitlementSpecs::likeBusinessUnitUserId),
            notBlank(criteria.getBusinessUnitId()).map(UserEntitlementSpecs::equalsBusinessUnitId),
            notBlank(criteria.getBusinessUnitName()).map(UserEntitlementSpecs::likeBusinessUnitName),
            notBlank(criteria.getBusinessUnitType()).map(UserEntitlementSpecs::likeBusinessUnitType),
            notBlank(criteria.getParentBusinessUnitId()).map(UserEntitlementSpecs::equalsParentBusinessUnitId),
            notBlank(criteria.getUserId()).map(UserEntitlementSpecs::likeUserId),
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
            businessUnitUserIdPredicate(joinBusinessUnitUser(root), builder, businessUnitUserId);
    }

    public static Specification<UserEntitlementEntity> equalsBusinessUnitId(String businessUnitId) {
        return (root, query, builder) ->
            businessUnitIdPredicate(joinBusinessUnit(joinBusinessUnitUser(root)), builder, businessUnitId);
    }

    public static Specification<UserEntitlementEntity> likeBusinessUnitName(String businessUnitName) {
        return (root, query, builder) ->
            businessUnitNamePredicate(joinBusinessUnit(joinBusinessUnitUser(root)), builder, businessUnitName);
    }

    public static Specification<UserEntitlementEntity> likeBusinessUnitType(String businessUnitType) {
        return (root, query, builder) ->
            businessUnitTypePredicate(joinBusinessUnit(joinBusinessUnitUser(root)), builder, businessUnitType);
    }

    public static Specification<UserEntitlementEntity> equalsParentBusinessUnitId(String parentBusinessUnitId) {
        return (root, query, builder) ->
            parentBusinessUnitIdPredicate(joinBusinessUnit(joinBusinessUnitUser(root)), builder, parentBusinessUnitId);
    }

    public static Specification<UserEntitlementEntity> likeUserId(String userId) {
        return (root, query, builder) -> userIdPredicate(joinUser(joinBusinessUnitUser(root)), builder, userId);
    }

    public static Specification<UserEntitlementEntity> likeUsername(String username) {
        return (root, query, builder) -> usernamePredicate(joinUser(joinBusinessUnitUser(root)), builder, username);
    }

    public static Specification<UserEntitlementEntity> equalsUsername(String username) {
        return (root, query, builder) -> equalsUsernamePredicate(
            joinUser(joinBusinessUnitUser(root)), builder, username);
    }

    public static Specification<UserEntitlementEntity> likeUserDescription(String description) {
        return (root, query, builder) -> userDescriptionPredicate(joinUser(joinBusinessUnitUser(root)), builder,
                                                                  description);
    }

    public static Specification<UserEntitlementEntity> likeFunctionName(String functionName) {
        return (root, query, builder) -> functionNamePredicate(joinApplicationFunction(root), builder, functionName);
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
