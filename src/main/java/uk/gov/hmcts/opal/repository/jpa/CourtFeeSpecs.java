package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.CourtFeeSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.CourtFeeEntity;
import uk.gov.hmcts.opal.entity.CourtFeeEntity_;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.businessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.businessUnitNamePredicate;

public class CourtFeeSpecs extends EntitySpecs<CourtFeeEntity> {

    public Specification<CourtFeeEntity> findBySearchCriteria(CourtFeeSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericLong(criteria.getCourtFeeId()).map(CourtFeeSpecs::equalsCourtFeeId),
            numericShort(criteria.getBusinessUnitId()).map(CourtFeeSpecs::equalsBusinessUnitId),
            notBlank(criteria.getBusinessUnitName()).map(CourtFeeSpecs::likeBusinessUnitName),
            notBlank(criteria.getCourtFeeCode()).map(CourtFeeSpecs::likeCourtFeeCode),
            notBlank(criteria.getDescription()).map(CourtFeeSpecs::likeDescription),
            notBlank(criteria.getStatsCode()).map(CourtFeeSpecs::likeStatsCode)
        ));
    }

    public static Specification<CourtFeeEntity> equalsCourtFeeId(Long courtFeeId) {
        return (root, query, builder) -> builder.equal(root.get(CourtFeeEntity_.courtFeeId), courtFeeId);
    }

    public static Specification<CourtFeeEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            businessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Specification<CourtFeeEntity> likeBusinessUnitName(String businessUnitName) {
        return (root, query, builder) ->
            businessUnitNamePredicate(joinBusinessUnit(root), builder, businessUnitName);
    }

    public static Specification<CourtFeeEntity> likeCourtFeeCode(String courtFeeCode) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CourtFeeEntity_.courtFeeCode), builder, courtFeeCode);
    }

    public static Specification<CourtFeeEntity> likeDescription(String description) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CourtFeeEntity_.description), builder, description);
    }

    public static Specification<CourtFeeEntity> likeStatsCode(String statsCode) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CourtFeeEntity_.statsCode), builder, statsCode);
    }

    public static Join<CourtFeeEntity, BusinessUnitEntity> joinBusinessUnit(From<?, CourtFeeEntity> from) {
        return from.join(CourtFeeEntity_.businessUnit);
    }

}
