package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.UserEntitlementSearchDto;
import uk.gov.hmcts.opal.entity.UserEntitlementEntity;
import uk.gov.hmcts.opal.entity.UserEntitlementEntity_;

public class UserEntitlementSpecs extends EntitySpecs<UserEntitlementEntity> {

    public Specification<UserEntitlementEntity> findBySearchCriteria(UserEntitlementSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getUserEntitlementId()).map(UserEntitlementSpecs::equalsUserEntitlementId)
        ));
    }

    public static Specification<UserEntitlementEntity> equalsUserEntitlementId(String userEntitlementId) {
        return (root, query, builder) -> builder.equal(root.get(UserEntitlementEntity_.userEntitlementId),
                                                       userEntitlementId);
    }

}
