package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.BacsPaymentSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.BacsPaymentEntity;
import uk.gov.hmcts.opal.entity.BacsPaymentEntity_;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;

public class BacsPaymentSpecs extends EntitySpecs<BacsPaymentEntity> {

    public Specification<BacsPaymentEntity> findBySearchCriteria(BacsPaymentSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getBacsPaymentId()).map(BacsPaymentSpecs::equalsBacsPaymentId),
            numericShort(criteria.getBusinessUnitId()).map(BacsPaymentSpecs::equalsBusinessUnitId)
        ));
    }

    public static Specification<BacsPaymentEntity> equalsBacsPaymentId(String bacsPaymentId) {
        return (root, query, builder) -> builder.equal(root.get(BacsPaymentEntity_.bacsPaymentId), bacsPaymentId);
    }

    public static Specification<BacsPaymentEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Join<BacsPaymentEntity, BusinessUnitEntity> joinBusinessUnit(From<?, BacsPaymentEntity> from) {
        return from.join(BacsPaymentEntity_.businessUnit);
    }
}
