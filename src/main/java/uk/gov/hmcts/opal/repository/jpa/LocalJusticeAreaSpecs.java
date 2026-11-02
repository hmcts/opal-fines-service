package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.hibernate.query.criteria.JpaExpression;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity_;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaType;

public class LocalJusticeAreaSpecs extends AddressSpecs<LocalJusticeAreaEntity> {

    public Specification<LocalJusticeAreaEntity> referenceDataFilter(
        String filter, List<uk.gov.hmcts.opal.generated.model.LocalJusticeAreaType> ljaTypes,
        LocalDateTime currentDateTime) {

        Optional<Specification<LocalJusticeAreaEntity>> ljaTypeSpec =
            notNullOrEmpty(ljaTypes).map(this::containsLocalJusticeAreaTypes);
        Optional<Specification<LocalJusticeAreaEntity>> filterSpec =
            notBlank(filter).map(this::likeAnyLocalJusticeArea);

        return Specification.allOf(specificationList(
            List.of(filterSpec, ljaTypeSpec), endDateGreaterThenEqualToDate(currentDateTime)));
    }

    public static Specification<LocalJusticeAreaEntity> equalsLocalJusticeAreaId(Short localJusticeAreaId) {
        return (root, query, builder) -> equalsLocalJusticeAreaIdPredicate(root, builder, localJusticeAreaId);
    }

    public static Predicate equalsLocalJusticeAreaIdPredicate(From<?, LocalJusticeAreaEntity> from,
                                                              CriteriaBuilder builder, Short localJusticeAreaId) {
        return builder.equal(from.get(LocalJusticeAreaEntity_.localJusticeAreaId), localJusticeAreaId);
    }

    public static Specification<LocalJusticeAreaEntity> likeLocalJusticeAreaId(String localJusticeAreaId) {
        return (root, query, builder) ->
            likeWildcardPredicate(((JpaExpression<?>) root.get(LocalJusticeAreaEntity_.localJusticeAreaId))
                .cast(String.class), builder, localJusticeAreaId);
    }

    public Specification<LocalJusticeAreaEntity> likeAnyLocalJusticeArea(String filter) {
        return Specification.anyOf(
            likeLocalJusticeAreaId(filter),
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
        Collection<uk.gov.hmcts.opal.generated.model.LocalJusticeAreaType> ljaTypes) {

        List<LocalJusticeAreaType> parsedLjaTypes = ljaTypes.stream()
            .map(Enum::name)
            .map(LocalJusticeAreaType::valueOf)
            .toList();

        return (root, query, builder) -> root.get(LocalJusticeAreaEntity_.ljaType).in(parsedLjaTypes);
    }
}
