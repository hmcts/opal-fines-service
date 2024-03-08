package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.UserSearchDto;
import uk.gov.hmcts.opal.entity.UserEntity;
import uk.gov.hmcts.opal.entity.UserEntity_;

public class UserSpecs extends EntitySpecs<UserEntity> {

    public Specification<UserEntity> findBySearchCriteria(UserSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getUserId()).map(UserSpecs::likeUserId),
            notBlank(criteria.getUsername()).map(UserSpecs::likeUsername),
            notBlank(criteria.getDescription()).map(UserSpecs::likeUserDescription)
        ));
    }

    public Specification<UserEntity> findByUserIdOrName(String userIdOrName) {
        return Specification.allOf(specificationList(
            notBlank(userIdOrName).map(UserSpecs::equalsUserId),
            notBlank(userIdOrName).map(UserSpecs::equalsUsername)
        ));
    }

    public static Specification<UserEntity> equalsUserId(String userId) {
        return (root, query, builder) -> builder.equal(root.get(UserEntity_.userId), userId);
    }

    public static Specification<UserEntity> likeUserId(String userId) {
        return (root, query, builder) -> userIdPredicate(root, builder, userId);
    }

    public static Predicate userIdPredicate(From<?, UserEntity> from, CriteriaBuilder builder, String userId) {
        return likeWildcardPredicate(from.get(UserEntity_.userId), builder, userId);
    }

    public static Specification<UserEntity> equalsUsername(String username) {
        return (root, query, builder) -> equalsUsernamePredicate(root, builder, username);
    }

    public static Predicate equalsUsernamePredicate(From<?, UserEntity> from, CriteriaBuilder builder,
                                                    String username) {
        return builder.equal(from.get(UserEntity_.username), username);
    }

    public static Specification<UserEntity> likeUsername(String username) {
        return (root, query, builder) -> usernamePredicate(root, builder, username);
    }

    public static Predicate usernamePredicate(From<?, UserEntity> from, CriteriaBuilder builder, String username) {
        return likeWildcardPredicate(from.get(UserEntity_.username), builder, username);
    }

    public static Specification<UserEntity> likeUserDescription(String description) {
        return (root, query, builder) -> userDescriptionPredicate(root, builder, description);
    }

    public static Predicate userDescriptionPredicate(From<?, UserEntity> from,
                                                     CriteriaBuilder builder, String description) {
        return likeWildcardPredicate(from.get(UserEntity_.description), builder, description);
    }
}
