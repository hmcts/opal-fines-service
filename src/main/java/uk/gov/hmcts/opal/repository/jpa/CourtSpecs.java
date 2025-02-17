package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity_;

import java.util.Optional;


public class CourtSpecs extends AddressCySpecs<CourtEntity.Lite> {

    public Specification<CourtEntity.Lite> findBySearchCriteria(CourtSearchDto criteria) {
        return Specification.allOf(specificationList(
            findByBaseCourtCriteria(criteria),
            numericLong(criteria.getCourtId()).map(CourtSpecs::equalsCourtId),
            notBlank(criteria.getCourtCode()).map(CourtSpecs::equalsCourtCode),
            numericLong(criteria.getParentCourtId()).map(CourtSpecs::equalsParentCourtId),
            numericShort(criteria.getLocalJusticeAreaId()).map(CourtSpecs::equalsLocalJusticeAreaId),
            notBlank(criteria.getNationalCourtCode()).map(CourtSpecs::likeNationalCourtCode)
        ));
    }

    public Specification<CourtEntity.Lite> referenceDataFilter(Optional<String> filter,
                                                               Optional<Short> businessUnitId) {
        return Specification.allOf(specificationList(
            filter.filter(s -> !s.isBlank()).map(this::likeAnyCourt),
            businessUnitId.map(CourtSpecs::equalsBusinessUnitId)
        ));
    }

    public static Specification<CourtEntity.Lite> equalsCourtId(Long courtId) {
        return (root, query, builder) -> equalsCourtIdPredicate(root, builder, courtId);
    }

    public static Predicate equalsCourtIdPredicate(From<?, CourtEntity.Lite> from, CriteriaBuilder builder,
                                                   Long courtId) {
        return builder.equal(from.get(CourtEntity_.courtId), courtId);
    }

    public static Specification<CourtEntity.Lite> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->  builder.equal(root.get(CourtEntity_.businessUnitId), businessUnitId);
    }

    public static Specification<CourtEntity.Lite> equalsCourtCode(String courtCode) {
        return (root, query, builder) -> builder.equal(root.get(CourtEntity_.courtCode), courtCode);
    }

    public static Specification<CourtEntity.Lite> equalsParentCourtId(Long parentCourtId) {
        return (root, query, builder) -> builder.equal(root.get(CourtEntity_.parentCourtId), parentCourtId);
        // return (root, query, builder) -> equalsCourtIdPredicate(joinParentCourt(root), builder, parentCourtId);
    }

    public static Specification<CourtEntity.Lite> equalsLocalJusticeAreaId(Short localJusticeAreaId) {
        return (root, query, builder) -> builder.equal(root.get(CourtEntity_.localJusticeAreaId),
                                                       localJusticeAreaId);
    }

    public static Specification<CourtEntity.Lite> likeNationalCourtCode(String nationalCourtCode) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CourtEntity_.nationalCourtCode), builder, nationalCourtCode);
    }

    public Specification<CourtEntity.Lite> likeAnyCourt(String filter) {
        return Specification.anyOf(
            likeName(filter),
            likeNameCy(filter),
            likeNationalCourtCode(filter)
        );
    }
}
