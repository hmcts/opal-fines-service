package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.HmrcRequestSearchDto;
import uk.gov.hmcts.opal.entity.HmrcRequestEntity;
import uk.gov.hmcts.opal.entity.HmrcRequestEntity_;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnit;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitLiteSpecs.equalsBusinessUnitIdPredicate;

public class HmrcRequestSpecs extends EntitySpecs<HmrcRequestEntity> {

    public Specification<HmrcRequestEntity> findBySearchCriteria(HmrcRequestSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getHmrcRequestId()).map(HmrcRequestSpecs::equalsHmrcRequestId),
            numericShort(criteria.getBusinessUnitId()).map(HmrcRequestSpecs::equalsBusinessUnitId)
        ));
    }

    public static Specification<HmrcRequestEntity> equalsHmrcRequestId(String hmrcRequestId) {
        return (root, query, builder) -> builder.equal(root.get(HmrcRequestEntity_.hmrcRequestId), hmrcRequestId);
    }

    public static Specification<HmrcRequestEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Join<HmrcRequestEntity, BusinessUnit.Lite> joinBusinessUnit(From<?, HmrcRequestEntity> from) {
        return from.join(HmrcRequestEntity_.businessUnit);
    }
}
