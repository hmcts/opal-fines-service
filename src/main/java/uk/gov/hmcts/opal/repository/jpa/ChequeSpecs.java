package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.ChequeSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.ChequeEntity;
import uk.gov.hmcts.opal.entity.ChequeEntity_;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;

public class ChequeSpecs extends EntitySpecs<ChequeEntity> {

    public Specification<ChequeEntity> findBySearchCriteria(ChequeSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getChequeId()).map(ChequeSpecs::equalsChequeId),
            numericShort(criteria.getBusinessUnitId()).map(ChequeSpecs::equalsBusinessUnitId)
        ));
    }

    public static Specification<ChequeEntity> equalsChequeId(String chequeId) {
        return (root, query, builder) -> builder.equal(root.get(ChequeEntity_.chequeId), chequeId);
    }

    public static Specification<ChequeEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Specification<ChequeEntity> equalsChequeNumber(String chequeNumber) {
        return (root, query, builder) -> builder.equal(root.get(ChequeEntity_.chequeNumber), chequeNumber);
    }

    public static Join<ChequeEntity, BusinessUnitEntity> joinBusinessUnit(From<?, ChequeEntity> from) {
        return from.join(ChequeEntity_.businessUnit);
    }
}
