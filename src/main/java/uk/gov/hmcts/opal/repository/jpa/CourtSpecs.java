package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.CourtEntity;
import uk.gov.hmcts.opal.entity.CourtEntity_;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;

import java.util.Optional;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.LocalJusticeAreaSpecs.equalsLocalJusticeAreaIdPredicate;

public class CourtSpecs extends AddressCySpecs<CourtEntity> {

    public Specification<CourtEntity> findBySearchCriteria(CourtSearchDto criteria) {
        return Specification.allOf(specificationList(
            findByBaseCourtCriteria(criteria),
            numericLong(criteria.getCourtId()).map(CourtSpecs::equalsCourtId),
            notBlank(criteria.getCourtCode()).map(CourtSpecs::equalsCourtCode),
            numericLong(criteria.getParentCourtId()).map(CourtSpecs::equalsParentCourtId),
            numericShort(criteria.getLocalJusticeAreaId()).map(CourtSpecs::equalsLocalJusticeAreaId),
            numericShort(criteria.getBusinessUnitId()).map(CourtSpecs::equalsBusinessUnitId),
            notBlank(criteria.getNationalCourtCode()).map(CourtSpecs::likeNationalCourtCode)
        ));
    }

    public Specification<CourtEntity> referenceDataFilter(Optional<String> filter,
                                                          Optional<Short> businessUnitId) {
        return Specification.allOf(specificationList(
            filter.filter(s -> !s.isBlank()).map(this::likeAnyCourt),
            businessUnitId.map(CourtSpecs::equalsBusinessUnitId)
        ));
    }

    public static Specification<CourtEntity> equalsCourtId(Long courtId) {
        return (root, query, builder) -> equalsCourtIdPredicate(root, builder, courtId);
    }

    public static Predicate equalsCourtIdPredicate(From<?, CourtEntity> from, CriteriaBuilder builder, Long courtId) {
        return builder.equal(from.get(CourtEntity_.courtId), courtId);
    }

    public static Specification<CourtEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Specification<CourtEntity> equalsCourtCode(String courtCode) {
        return (root, query, builder) -> builder.equal(root.get(CourtEntity_.courtCode), courtCode);
    }

    public static Specification<CourtEntity> equalsParentCourtId(Long parentCourtId) {
        return (root, query, builder) -> equalsCourtIdPredicate(joinParentCourt(root), builder, parentCourtId);
    }

    public static Specification<CourtEntity> equalsLocalJusticeAreaId(Short localJusticeAreaId) {
        return (root, query, builder) -> equalsLocalJusticeAreaIdPredicate(joinLocalJusticeArea(root), builder,
                                                                           localJusticeAreaId);
    }

    public static Specification<CourtEntity> likeNationalCourtCode(String nationalCourtCode) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CourtEntity_.nationalCourtCode), builder, nationalCourtCode);
    }

    public Specification<CourtEntity> likeAnyCourt(String filter) {
        return Specification.anyOf(
            likeName(filter),
            likeNameCy(filter),
            likeNationalCourtCode(filter)
        );
    }

    public static Join<CourtEntity, BusinessUnitEntity> joinBusinessUnit(From<?, CourtEntity> from) {
        return from.join(CourtEntity_.businessUnit);
    }

    public static Join<CourtEntity, CourtEntity> joinParentCourt(From<?, CourtEntity> from) {
        return from.join(CourtEntity_.parentCourt);
    }

    public static Join<CourtEntity, LocalJusticeAreaEntity> joinLocalJusticeArea(From<?, CourtEntity> from) {
        return from.join(CourtEntity_.localJusticeArea);
    }

}
