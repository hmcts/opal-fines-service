package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.EnforcerSearchDto;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.EnforcerEntity_;

public class EnforcerSpecs extends EntitySpecs<EnforcerEntity> {

    public Specification<EnforcerEntity> findBySearchCriteria(EnforcerSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getEnforcerId()).map(EnforcerSpecs::equalsEnforcerId)
        ));
    }

    public static Specification<EnforcerEntity> equalsEnforcerId(String enforcerId) {
        return (root, query, builder) -> builder.equal(root.get(EnforcerEntity_.enforcerId), enforcerId);
    }

}
