package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.WarrantRegisterSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.WarrantRegisterEntity;
import uk.gov.hmcts.opal.entity.WarrantRegisterEntity_;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;

public class WarrantRegisterSpecs extends EntitySpecs<WarrantRegisterEntity> {

    public Specification<WarrantRegisterEntity> findBySearchCriteria(WarrantRegisterSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getWarrantRegisterId()).map(WarrantRegisterSpecs::equalsWarrantRegisterId),
            numericShort(criteria.getBusinessUnitId()).map(WarrantRegisterSpecs::equalsBusinessUnitId)
        ));
    }

    public static Specification<WarrantRegisterEntity> equalsWarrantRegisterId(String warrantRegisterId) {
        return (root, query, builder) -> builder.equal(root.get(WarrantRegisterEntity_.warrantRegisterId),
                                                       warrantRegisterId);
    }

    public static Specification<WarrantRegisterEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Join<WarrantRegisterEntity, BusinessUnitEntity> joinBusinessUnit(
        From<?, WarrantRegisterEntity> from) {
        return from.join(WarrantRegisterEntity_.businessUnit);
    }
}
