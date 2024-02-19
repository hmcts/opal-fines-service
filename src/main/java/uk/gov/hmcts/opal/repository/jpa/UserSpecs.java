package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.UserSearchDto;
import uk.gov.hmcts.opal.entity.UserEntity;
import uk.gov.hmcts.opal.entity.UserEntity_;

public class UserSpecs extends EntitySpecs<UserEntity> {

    public Specification<UserEntity> findBySearchCriteria(UserSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getUserId()).map(UserSpecs::equalsUserId)
        ));
    }

    public static Specification<UserEntity> equalsUserId(String userId) {
        return (root, query, builder) -> builder.equal(root.get(UserEntity_.userId), userId);
    }

}
