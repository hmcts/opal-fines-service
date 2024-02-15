package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.BusinessUnitUserSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitUserEntity;
import uk.gov.hmcts.opal.entity.BusinessUnitUserEntity_;

public class BusinessUnitUserSpecs extends EntitySpecs<BusinessUnitUserEntity> {

    public Specification<BusinessUnitUserEntity> findBySearchCriteria(BusinessUnitUserSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getBusinessUnitUserId()).map(BusinessUnitUserSpecs::equalsBusinessUnitUserId)
        ));
    }

    public static Specification<BusinessUnitUserEntity> equalsBusinessUnitUserId(String businessUnitUserId) {
        return (root, query, builder) -> builder.equal(root.get(BusinessUnitUserEntity_.businessUnitUserId),
                                                       businessUnitUserId);
    }

}
