package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity_;

public class BusinessUnitSpecs extends EntitySpecs<BusinessUnitEntity> {

    public Specification<BusinessUnitEntity> findBySearchCriteria(BusinessUnitSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getBusinessUnitId()).map(BusinessUnitSpecs::equalsBusinessUnitId),
            notBlank(criteria.getBusinessUnitName()).map(BusinessUnitSpecs::equalsBusinessUnitName),
            notBlank(criteria.getBusinessUnitCode()).map(BusinessUnitSpecs::equalsBusinessUnitCode),
            notBlank(criteria.getBusinessUnitType()).map(BusinessUnitSpecs::equalsBusinessUnitType),
            notBlank(criteria.getAccountNumberPrefix()).map(BusinessUnitSpecs::equalsAccountNumberPrefix),
            notBlank(criteria.getParentBusinessUnitId()).map(BusinessUnitSpecs::equalsParentBusinessUnitId)
        ));
    }

    public static Specification<BusinessUnitEntity> equalsBusinessUnitId(String businessUnitId) {
        return (root, query, builder) -> builder.equal(root.get(BusinessUnitEntity_.businessUnitId), businessUnitId);
    }

    public static Specification<BusinessUnitEntity> equalsBusinessUnitName(String businessUnitName) {
        return (root, query, builder) -> builder.equal(root.get(BusinessUnitEntity_.businessUnitName),
                                                       businessUnitName);
    }

    public static Specification<BusinessUnitEntity> equalsBusinessUnitCode(String businessUnitCode) {
        return (root, query, builder) -> builder.equal(root.get(BusinessUnitEntity_.businessUnitCode),
                                                       businessUnitCode);
    }

    public static Specification<BusinessUnitEntity> equalsBusinessUnitType(String businessUnitType) {
        return (root, query, builder) -> builder.equal(root.get(BusinessUnitEntity_.businessUnitType),
                                                       businessUnitType);
    }

    public static Specification<BusinessUnitEntity> equalsAccountNumberPrefix(String accountNumberPrefix) {
        return (root, query, builder) -> builder.equal(root.get(BusinessUnitEntity_.accountNumberPrefix),
                                                       accountNumberPrefix);
    }

    public static Specification<BusinessUnitEntity> equalsParentBusinessUnitId(String parentBusinessUnitId) {
        return (root, query, builder) -> builder.equal(root.get(BusinessUnitEntity_.parentBusinessUnitId),
                                                       parentBusinessUnitId);
    }

}
