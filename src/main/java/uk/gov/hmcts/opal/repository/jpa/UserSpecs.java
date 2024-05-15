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
            numericLong(criteria.getUserId()).map(UserSpecs::equalsUserId),
            notBlank(criteria.getUsername()).map(UserSpecs::likeUsername),
            notBlank(criteria.getDescription()).map(UserSpecs::likeUserDescription)
        ));
    }

    public Specification<UserEntity> findByUserIdOrName(String userIdOrName) {
        return Specification.allOf(specificationList(
            numericLong(userIdOrName).map(UserSpecs::equalsUserId),
            notBlank(userIdOrName).map(UserSpecs::likeUsername)
        ));
    }

    public static Specification<UserEntity> equalsUserId(Long userId) {
        return (root, query, builder) -> equalsUserIdPredicate(root, builder, userId);
    }

    public static Predicate equalsUserIdPredicate(From<?, UserEntity> from, CriteriaBuilder builder, Long userId) {
        return builder.equal(from.get(UserEntity_.userId), userId);
    }

    public static Specification<UserEntity> likeUsername(String username) {
        return (root, query, builder) -> likeUsernamePredicate(root, builder, username);
    }

    public static Predicate likeUsernamePredicate(From<?, UserEntity> from, CriteriaBuilder builder, String username) {
        return likeWildcardPredicate(from.get(UserEntity_.username), builder, username);
    }

    public static Specification<UserEntity> likeUserDescription(String description) {
        return (root, query, builder) -> likeUserDescriptionPredicate(root, builder, description);
    }

    public static Predicate likeUserDescriptionPredicate(From<?, UserEntity> from,
                                                         CriteriaBuilder builder, String description) {
        return likeWildcardPredicate(from.get(UserEntity_.description), builder, description);
    }
}
