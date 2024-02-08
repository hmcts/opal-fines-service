package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity_;

public class BusinessUnitSpecs extends EntitySpecs<BusinessUnitEntity> {

    public Specification<BusinessUnitEntity> findBySearchCriteria(BusinessUnitSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getBusinessUnitId()).map(BusinessUnitSpecs::equalsBusinessUnitId)
        ));
    }

    public static Specification<BusinessUnitEntity> equalsBusinessUnitId(String businessUnitId) {
        return (root, query, builder) -> builder.equal(root.get(BusinessUnitEntity_.businessUnitId), businessUnitId);
    }

}
