package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.ControlTotalSearchDto;
import uk.gov.hmcts.opal.entity.ControlTotalEntity;
import uk.gov.hmcts.opal.entity.ControlTotalEntity_;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnit;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitLiteSpecs.equalsBusinessUnitIdPredicate;

public class ControlTotalSpecs extends EntitySpecs<ControlTotalEntity> {

    public Specification<ControlTotalEntity> findBySearchCriteria(ControlTotalSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getControlTotalId()).map(ControlTotalSpecs::equalsControlTotalId),
            numericShort(criteria.getBusinessUnitId()).map(ControlTotalSpecs::equalsBusinessUnitId)
        ));
    }

    public static Specification<ControlTotalEntity> equalsControlTotalId(String controlTotalId) {
        return (root, query, builder) -> builder.equal(root.get(ControlTotalEntity_.controlTotalId), controlTotalId);
    }

    public static Specification<ControlTotalEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Join<ControlTotalEntity, BusinessUnit.Lite> joinBusinessUnit(From<?, ControlTotalEntity> from) {
        return from.join(ControlTotalEntity_.businessUnit);
    }
}
