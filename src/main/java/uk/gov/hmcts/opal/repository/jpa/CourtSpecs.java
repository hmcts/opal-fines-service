package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.court.CourtLegacyEntity;
import uk.gov.hmcts.opal.entity.court.CourtLegacyEntity_;

import java.util.Optional;

public class CourtSpecs extends AddressCySpecs<CourtLegacyEntity> {

    public Specification<CourtLegacyEntity> findBySearchCriteria(CourtSearchDto criteria) {
        return Specification.allOf(specificationList(
            findByAddressCyCriteria(criteria),
            numericLong(criteria.getCourtId()).map(CourtSpecs::equalsCourtId),
            notBlank(criteria.getCourtCode()).map(CourtSpecs::equalsCourtCode),
            numericShort(criteria.getLocalJusticeAreaId()).map(CourtSpecs::equalsLocalJusticeAreaId),
            numericShort(criteria.getBusinessUnitId()).map(CourtSpecs::equalsBusinessUnitId)
        ));
    }

    public Specification<CourtLegacyEntity> referenceDataFilter(Optional<String> filter,
                                                          Optional<Short> businessUnitId) {
        return Specification.allOf(specificationList(
            filter.filter(s -> !s.isBlank()).map(this::likeAnyCourt),
            businessUnitId.map(CourtSpecs::equalsBusinessUnitId)
        ));
    }

    public static Specification<CourtLegacyEntity> equalsCourtId(Long courtId) {
        return (root, query, builder) -> equalsCourtIdPredicate(root, builder, courtId);
    }

    public static Predicate equalsCourtIdPredicate(From<?, CourtLegacyEntity> from,
                                                   CriteriaBuilder builder, Long courtId) {
        return builder.equal(from.get(CourtLegacyEntity_.courtId), courtId);
    }

    public static Specification<CourtLegacyEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            builder.equal(root.get(CourtLegacyEntity_.businessUnitId), businessUnitId);
    }

    public static Specification<CourtLegacyEntity> equalsCourtCode(String courtCode) {
        return (root, query, builder) -> builder.equal(root.get(CourtLegacyEntity_.courtCode), courtCode);
    }

    public static Specification<CourtLegacyEntity> equalsLocalJusticeAreaId(Short localJusticeAreaId) {
        return (root, query, builder) ->
            builder.equal(root.get(CourtLegacyEntity_.localJusticeAreaId), localJusticeAreaId);
    }

    public Specification<CourtLegacyEntity> likeAnyCourt(String filter) {
        return Specification.anyOf(
            likeName(filter),
            likeNameCy(filter)
        );
    }
}
