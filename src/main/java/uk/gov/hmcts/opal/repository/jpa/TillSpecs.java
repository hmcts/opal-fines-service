package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.TillSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.TillEntity_;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;

public class TillSpecs extends EntitySpecs<TillEntity> {

    public Specification<TillEntity> findBySearchCriteria(TillSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getTillId()).map(TillSpecs::equalsTillId),
            numericShort(criteria.getBusinessUnitId()).map(TillSpecs::equalsBusinessUnitId),
            notBlank(criteria.getTillNumber()).map(TillSpecs::equalsTillNumber),
            notBlank(criteria.getOwnedBy()).map(TillSpecs::equalsOwnedBy)
        ));
    }

    public static Specification<TillEntity> equalsTillId(String tillId) {
        return (root, query, builder) -> builder.equal(root.get(TillEntity_.tillId), tillId);
    }

    public static Specification<TillEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Specification<TillEntity> equalsTillNumber(String tillNumber) {
        return (root, query, builder) -> builder.equal(root.get(TillEntity_.tillNumber), tillNumber);
    }

    public static Specification<TillEntity> equalsOwnedBy(String ownedBy) {
        return (root, query, builder) -> builder.equal(root.get(TillEntity_.ownedBy), ownedBy);
    }

    public static Join<TillEntity, BusinessUnitEntity> joinBusinessUnit(From<?, TillEntity> from) {
        return from.join(TillEntity_.businessUnit);
    }
}
