package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.ApplicationFunctionSearchDto;
import uk.gov.hmcts.opal.entity.ApplicationFunctionEntity;
import uk.gov.hmcts.opal.entity.ApplicationFunctionEntity_;

public class ApplicationFunctionSpecs extends EntitySpecs<ApplicationFunctionEntity> {

    public Specification<ApplicationFunctionEntity> findBySearchCriteria(ApplicationFunctionSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getApplicationFunctionId()).map(ApplicationFunctionSpecs::equalsApplicationFunctionId)
        ));
    }

    public static Specification<ApplicationFunctionEntity> equalsApplicationFunctionId(String applicationFunctionId) {
        return (root, query, builder) -> builder.equal(root.get(ApplicationFunctionEntity_.applicationFunctionId),
                                                       applicationFunctionId);
    }

}
