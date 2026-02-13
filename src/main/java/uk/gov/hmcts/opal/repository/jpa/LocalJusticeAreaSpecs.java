package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity_;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class LocalJusticeAreaSpecs extends AddressSpecs<LocalJusticeAreaEntity> {

    public Specification<LocalJusticeAreaEntity> findBySearchCriteria(LocalJusticeAreaSearchDto criteria) {
        return Specification.allOf(specificationList(
            findByAddressCriteria(criteria),
            notBlank(criteria.getLjaCode()).map(LocalJusticeAreaSpecs::likeLjaCode),
            numericShort(criteria.getLocalJusticeAreaId()).map(LocalJusticeAreaSpecs::equalsLocalJusticeAreaId)
        ));
    }

    public Specification<LocalJusticeAreaEntity> referenceDataFilter(Optional<String> filter,
        Optional<List<String>> ljaTypesFilter) {

        Optional<Specification<LocalJusticeAreaEntity>> ljaTypeSpec =
            ljaTypesFilter.filter(s -> !s.isEmpty()).map(this::containsLocalJusticeAreaTypes);
        Optional<Specification<LocalJusticeAreaEntity>> filterSpec =
            filter.filter(s -> !s.isBlank()).map(this::likeAnyLocalJusticeArea);

        return Specification.allOf(specificationList(
            List.of(filterSpec, ljaTypeSpec), endDateGreaterThenEqualToDate(LocalDateTime.now())));
    }

    public static Specification<LocalJusticeAreaEntity> equalsLocalJusticeAreaId(Short localJusticeAreaId) {
        return (root, query, builder) -> equalsLocalJusticeAreaIdPredicate(root, builder, localJusticeAreaId);
    }

    public static Predicate equalsLocalJusticeAreaIdPredicate(From<?, LocalJusticeAreaEntity> from,
                                                              CriteriaBuilder builder, Short localJusticeAreaId) {
        return builder.equal(from.get(LocalJusticeAreaEntity_.localJusticeAreaId), localJusticeAreaId);
    }

    public static Specification<LocalJusticeAreaEntity> likeLjaCode(String ljaCode) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(LocalJusticeAreaEntity_.ljaCode), builder, ljaCode);
    }

    public Specification<LocalJusticeAreaEntity> likeAnyLocalJusticeArea(String filter) {
        return Specification.anyOf(
            likeLjaCode(filter),
            likeName(filter),
            likePostcode(filter)
        );
    }

    public static Specification<LocalJusticeAreaEntity> endDateGreaterThenEqualToDate(LocalDateTime expiryDate) {
        return (root, query, builder) -> builder.or(
            builder.isNull(root.get(LocalJusticeAreaEntity_.endDate)),
            builder.greaterThanOrEqualTo(root.get(LocalJusticeAreaEntity_.endDate), expiryDate)
        );
    }

    public Specification<LocalJusticeAreaEntity> containsLocalJusticeAreaTypes(
        List<String> ljaTypes) {
        return
            (root, query, builder) ->
                root.get(LocalJusticeAreaEntity_.ljaType).in(ljaTypes);
    }
}
