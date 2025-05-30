package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity_;

import java.util.Optional;

public class CourtSpecs extends AddressCySpecs<CourtEntity> {

    public Specification<CourtEntity> findBySearchCriteria(CourtSearchDto criteria) {
        return Specification.allOf(specificationList(
            findByAddressCyCriteria(criteria),
            numericLong(criteria.getCourtId()).map(CourtSpecs::equalsCourtId),
            notBlank(criteria.getCourtCode()).map(CourtSpecs::equalsCourtCode),
            numericShort(criteria.getLocalJusticeAreaId()).map(CourtSpecs::equalsLocalJusticeAreaId),
            numericShort(criteria.getBusinessUnitId()).map(CourtSpecs::equalsBusinessUnitId)
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
            builder.equal(root.get(CourtEntity_.businessUnitId), businessUnitId);
    }

    public static Specification<CourtEntity> equalsCourtCode(String courtCode) {
        return (root, query, builder) -> builder.equal(root.get(CourtEntity_.courtCode), courtCode);
    }

    public static Specification<CourtEntity> equalsLocalJusticeAreaId(Short localJusticeAreaId) {
        return (root, query, builder) ->
            builder.equal(root.get(CourtEntity_.localJusticeAreaId), localJusticeAreaId);
    }

    public Specification<CourtEntity> likeAnyCourt(String filter) {
        return Specification.anyOf(
            likeName(filter),
            likeNameCy(filter)
        );
    }
}
