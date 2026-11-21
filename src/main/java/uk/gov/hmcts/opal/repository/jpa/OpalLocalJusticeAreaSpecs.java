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
import uk.gov.hmcts.opal.entity.OpalLocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.OpalLocalJusticeAreaEntity_;
import uk.gov.hmcts.opal.entity.OpalLocalJusticeAreaType;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreaType;

public class OpalLocalJusticeAreaSpecs extends AddressSpecs<OpalLocalJusticeAreaEntity> {

    public Specification<OpalLocalJusticeAreaEntity> referenceDataFilter(
        String filter, List<LocalJusticeAreaType> ljaTypes, LocalDateTime currentDateTime) {

        Optional<Specification<OpalLocalJusticeAreaEntity>> ljaTypeSpec =
            notNullOrEmpty(ljaTypes).map(this::containsLocalJusticeAreaTypes);
        Optional<Specification<OpalLocalJusticeAreaEntity>> filterSpec =
            notBlank(filter).map(this::likeAnyLocalJusticeArea);

        return Specification.allOf(specificationList(
            List.of(filterSpec, ljaTypeSpec), endDateGreaterThenEqualToDate(currentDateTime)));
    }

    public static Specification<OpalLocalJusticeAreaEntity> equalsLocalJusticeAreaId(Short localJusticeAreaId) {
        return (root, query, builder) -> equalsLocalJusticeAreaIdPredicate(root, builder, localJusticeAreaId);
    }

    public static Predicate equalsLocalJusticeAreaIdPredicate(From<?, OpalLocalJusticeAreaEntity> from,
                                                              CriteriaBuilder builder, Short localJusticeAreaId) {
        return builder.equal(from.get(OpalLocalJusticeAreaEntity_.localJusticeAreaId), localJusticeAreaId);
    }

    public static Specification<OpalLocalJusticeAreaEntity> likeLocalJusticeAreaId(String localJusticeAreaId) {
        return (root, query, builder) ->
            likeWildcardPredicate(((JpaExpression<?>) root.get(OpalLocalJusticeAreaEntity_.localJusticeAreaId))
                .cast(String.class), builder, localJusticeAreaId);
    }

    public Specification<OpalLocalJusticeAreaEntity> likeAnyLocalJusticeArea(String filter) {
        return Specification.anyOf(
            likeLocalJusticeAreaId(filter),
            likeName(filter),
            likePostcode(filter)
        );
    }

    public static Specification<OpalLocalJusticeAreaEntity> endDateGreaterThenEqualToDate(LocalDateTime expiryDate) {
        return (root, query, builder) -> builder.or(
            builder.isNull(root.get(OpalLocalJusticeAreaEntity_.endDate)),
            builder.greaterThanOrEqualTo(root.get(OpalLocalJusticeAreaEntity_.endDate), expiryDate)
        );
    }

    public Specification<OpalLocalJusticeAreaEntity> containsLocalJusticeAreaTypes(
        Collection<LocalJusticeAreaType> ljaTypes) {

        List<OpalLocalJusticeAreaType> parsedLjaTypes = ljaTypes.stream()
            .map(Enum::name)
            .map(OpalLocalJusticeAreaType::valueOf)
            .toList();

        return (root, query, builder) -> root.get(OpalLocalJusticeAreaEntity_.ljaType).in(parsedLjaTypes);
    }
}
