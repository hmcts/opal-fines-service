package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LegacyLocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.LegacyLocalJusticeAreaEntity_;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaType;

public class LegacyLocalJusticeAreaSpecs extends AddressSpecs<LegacyLocalJusticeAreaEntity> {

    public Specification<LegacyLocalJusticeAreaEntity> findBySearchCriteria(LocalJusticeAreaSearchDto criteria) {
        return Specification.allOf(specificationList(
            findByAddressCriteria(criteria),
            notBlank(criteria.getLjaCode()).map(LegacyLocalJusticeAreaSpecs::likeLjaCode),
            numericShort(criteria.getLocalJusticeAreaId()).map(LegacyLocalJusticeAreaSpecs::equalsLocalJusticeAreaId)
        ));
    }

    public Specification<LegacyLocalJusticeAreaEntity> referenceDataFilter(Optional<String> filter,
        Optional<List<String>> ljaTypesFilter,
        LocalDateTime currentDateTime) {

        Optional<Specification<LegacyLocalJusticeAreaEntity>> ljaTypeSpec =
            ljaTypesFilter.filter(s -> !s.isEmpty()).map(this::containsLocalJusticeAreaTypes);
        Optional<Specification<LegacyLocalJusticeAreaEntity>> filterSpec =
            filter.filter(s -> !s.isBlank()).map(this::likeAnyLocalJusticeArea);

        return Specification.allOf(specificationList(
            List.of(filterSpec, ljaTypeSpec), endDateGreaterThenEqualToDate(currentDateTime)));
    }

    public static Specification<LegacyLocalJusticeAreaEntity> equalsLocalJusticeAreaId(Short localJusticeAreaId) {
        return (root, query, builder) -> equalsLocalJusticeAreaIdPredicate(root, builder, localJusticeAreaId);
    }

    public static Predicate equalsLocalJusticeAreaIdPredicate(From<?, LegacyLocalJusticeAreaEntity> from,
                                                              CriteriaBuilder builder,
                                                              Short localJusticeAreaId) {
        return builder.equal(from.get(LegacyLocalJusticeAreaEntity_.localJusticeAreaId), localJusticeAreaId);
    }

    public static Specification<LegacyLocalJusticeAreaEntity> likeLjaCode(String ljaCode) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(LegacyLocalJusticeAreaEntity_.ljaCode), builder, ljaCode);
    }

    public Specification<LegacyLocalJusticeAreaEntity> likeAnyLocalJusticeArea(String filter) {
        return Specification.anyOf(
            likeLjaCode(filter),
            likeName(filter),
            likePostcode(filter)
        );
    }

    public static Specification<LegacyLocalJusticeAreaEntity> endDateGreaterThenEqualToDate(LocalDateTime expiryDate) {
        return (root, query, builder) -> builder.or(
            builder.isNull(root.get(LegacyLocalJusticeAreaEntity_.endDate)),
            builder.greaterThanOrEqualTo(root.get(LegacyLocalJusticeAreaEntity_.endDate), expiryDate)
        );
    }

    public Specification<LegacyLocalJusticeAreaEntity> containsLocalJusticeAreaTypes(List<String> ljaTypes) {
        List<LocalJusticeAreaType> parsedLjaTypes = ljaTypes.stream()
            .flatMap(LegacyLocalJusticeAreaSpecs::parseLocalJusticeAreaType)
            .toList();

        return (root, query, builder) -> root.get(LegacyLocalJusticeAreaEntity_.ljaType).in(parsedLjaTypes);
    }

    private static Stream<LocalJusticeAreaType> parseLocalJusticeAreaType(String ljaType) {
        try {
            return Stream.of(LocalJusticeAreaType.valueOf(ljaType));
        } catch (IllegalArgumentException ex) {
            return Stream.empty();
        }
    }
}
