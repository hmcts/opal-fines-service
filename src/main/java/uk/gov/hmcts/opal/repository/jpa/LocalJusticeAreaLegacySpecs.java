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
import uk.gov.hmcts.opal.entity.LocalJusticeAreaLegacyEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaLegacyEntity_;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaType;

public class LocalJusticeAreaLegacySpecs extends AddressSpecs<LocalJusticeAreaLegacyEntity> {

    public Specification<LocalJusticeAreaLegacyEntity> findBySearchCriteria(
        LocalJusticeAreaSearchDto criteria) {

        return Specification.allOf(specificationList(
            findByAddressCriteria(criteria),
            notBlank(criteria.getLjaCode()).map(LocalJusticeAreaLegacySpecs::likeLjaCode),
            numericShort(criteria.getLocalJusticeAreaId())
                .map(LocalJusticeAreaLegacySpecs::equalsLocalJusticeAreaId)
        ));
    }

    public Specification<LocalJusticeAreaLegacyEntity> referenceDataFilter(
        Optional<String> filter,
        Optional<List<String>> ljaTypesFilter,
        LocalDateTime currentDateTime) {

        Optional<Specification<LocalJusticeAreaLegacyEntity>> ljaTypeSpec =
            ljaTypesFilter.filter(s -> !s.isEmpty())
                .map(this::containsLocalJusticeAreaTypes);

        Optional<Specification<LocalJusticeAreaLegacyEntity>> filterSpec =
            filter.filter(s -> !s.isBlank())
                .map(this::likeAnyLocalJusticeArea);

        return Specification.allOf(specificationList(
            List.of(filterSpec, ljaTypeSpec),
            endDateGreaterThenEqualToDate(currentDateTime)
        ));
    }

    public static Specification<LocalJusticeAreaLegacyEntity> equalsLocalJusticeAreaId(
        Short localJusticeAreaId) {

        return (root, query, builder) ->
            equalsLocalJusticeAreaIdPredicate(root, builder, localJusticeAreaId);
    }

    public static Predicate equalsLocalJusticeAreaIdPredicate(
        From<?, LocalJusticeAreaLegacyEntity> from,
        CriteriaBuilder builder,
        Short localJusticeAreaId) {

        return builder.equal(
            from.get(LocalJusticeAreaLegacyEntity_.localJusticeAreaId),
            localJusticeAreaId
        );
    }

    public static Specification<LocalJusticeAreaLegacyEntity> likeLjaCode(
        String ljaCode) {

        return (root, query, builder) ->
            likeWildcardPredicate(
                root.get(LocalJusticeAreaLegacyEntity_.ljaCode),
                builder,
                ljaCode
            );
    }

    public Specification<LocalJusticeAreaLegacyEntity> likeAnyLocalJusticeArea(
        String filter) {

        return Specification.anyOf(
            likeLjaCode(filter),
            likeName(filter),
            likePostcode(filter)
        );
    }

    public static Specification<LocalJusticeAreaLegacyEntity> endDateGreaterThenEqualToDate(
        LocalDateTime expiryDate) {

        return (root, query, builder) -> builder.or(
            builder.isNull(root.get(LocalJusticeAreaLegacyEntity_.endDate)),
            builder.greaterThanOrEqualTo(
                root.get(LocalJusticeAreaLegacyEntity_.endDate),
                expiryDate
            )
        );
    }

    public Specification<LocalJusticeAreaLegacyEntity> containsLocalJusticeAreaTypes(
        List<String> ljaTypes) {

        List<LocalJusticeAreaType> parsedLjaTypes = ljaTypes.stream()
            .flatMap(LocalJusticeAreaLegacySpecs::parseLocalJusticeAreaType)
            .toList();

        return (root, query, builder) ->
            root.get(LocalJusticeAreaLegacyEntity_.ljaType).in(parsedLjaTypes);
    }

    private static Stream<LocalJusticeAreaType> parseLocalJusticeAreaType(
        String ljaType) {

        try {
            return Stream.of(LocalJusticeAreaType.valueOf(ljaType));
        } catch (IllegalArgumentException ex) {
            return Stream.empty();
        }
    }
}