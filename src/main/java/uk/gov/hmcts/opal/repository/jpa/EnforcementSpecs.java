package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.EnforcementSearchDto;
import uk.gov.hmcts.opal.entity.EnforcementEntity;
import uk.gov.hmcts.opal.entity.EnforcementEntity_;

public class EnforcementSpecs extends EntitySpecs<EnforcementEntity> {

    public Specification<EnforcementEntity> findBySearchCriteria(EnforcementSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getEnforcementId()).map(EnforcementSpecs::equalsEnforcementId)
        ));
    }

    public static Specification<EnforcementEntity> equalsEnforcementId(String enforcementId) {
        return (root, query, builder) -> builder.equal(root.get(EnforcementEntity_.enforcementId), enforcementId);
    }

}
