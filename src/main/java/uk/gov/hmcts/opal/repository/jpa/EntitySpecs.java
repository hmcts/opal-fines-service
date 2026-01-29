package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.DateDto;


public abstract class EntitySpecs<E> {

    @SafeVarargs
    public final List<Specification<E>> specificationList(Optional<Specification<E>>... optionalSpecs) {
        return Arrays.stream(optionalSpecs)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }

    @SafeVarargs
    public final List<Specification<E>> specificationList(List<Optional<Specification<E>>> specsList,
                                                          Optional<Specification<E>>... optionalSpecs) {
        return combine(specsList, optionalSpecs)
            .stream().filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }

    @SafeVarargs
    public final List<Specification<E>> specificationList(List<Optional<Specification<E>>> specsList,
                                                          Specification<E>... specs) {
        List<Specification<E>> filteredList = specsList
            .stream().filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
        Collections.addAll(filteredList, specs);
        return filteredList;
    }

    @SafeVarargs
    public final List<Optional<Specification<E>>> combine(List<Optional<Specification<E>>> specsList,
                                                          Optional<Specification<E>>... optionalSpecs) {
        Collections.addAll(specsList, optionalSpecs);
        return specsList;
    }

    @SafeVarargs
    public final List<Predicate> predicateList(Optional<Predicate>... optionalPredicates) {
        return Arrays.stream(optionalPredicates)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }

    public final List<Predicate> predicateList(List<Predicate> predicates) {
        return predicates.stream()
            .filter(Objects::nonNull)
            .toList();
    }

    @SafeVarargs
    public final Predicate[] predicateArray(Optional<Predicate>... optionalPredicates) {
        return predicateList(optionalPredicates).toArray(new Predicate[] {});
    }

    public final Predicate[] predicateArray(List<Predicate> predicates) {
        return predicateList(predicates).toArray(new Predicate[] {});
    }

    public Optional<String> notBlank(String candidate) {
        return Optional.ofNullable(candidate).filter(s -> !s.isBlank());
    }

    public Optional<String> numeric(String candidate) {
        return notBlank(candidate).filter(s -> s.matches("\\d+"));
    }

    public Optional<Long> numericLong(String candidate) {
        return numeric(candidate).map(Long::parseLong);
    }

    public Optional<Integer> numericInteger(String candidate) {
        return numeric(candidate).map(Integer::parseInt);
    }

    public Optional<Short> numericShort(String candidate) {
        return numeric(candidate).map(Short::parseShort);
    }

    public Optional<Boolean> trueFalse(String candidate) {
        return notBlank(candidate).map(Boolean::parseBoolean);
    }

    public <T> Optional<T> notNullObject(T candidate) {
        return Optional.ofNullable(candidate);
    }

    public Optional<LocalDate> notNullLocalDate(DateDto candidate) {
        return Optional.ofNullable(candidate).map(DateDto::toLocalDate);
    }

    public Optional<LocalDateTime> notNullLocalDateTime(DateDto candidate) {
        return Optional.ofNullable(candidate).map(DateDto::toLocalDate).map(d -> d.atTime(0, 0, 0));
    }

    public <T> Optional<Collection<T>> notEmpty(Collection<T> collection) {
        return collection.isEmpty() ? Optional.empty() : Optional.of(collection);
    }

    public static Predicate likeWildcardPredicate(
        Expression<String> path, CriteriaBuilder cb, String candidate) {
        return likeLowerCaseBothPredicate(path, cb, "%" + candidate + "%");
    }

    public static Predicate likeLowerCaseWildcardPredicate(
        Expression<String> path, CriteriaBuilder cb, String candidate) {
        return likeLowerCasePredicate(path, cb, "%" + candidate + "%");
    }

    public static Predicate likeLowerCaseBothStartsWithPredicate(
        Expression<String> path, CriteriaBuilder cb, String candidate) {
        return likeLowerCaseBothPredicate(path, cb, candidate + "%");
    }

    public static Predicate likeLowerCaseBothPredicate(Expression<String> path, CriteriaBuilder cb, String candidate) {
        return cb.like(cb.lower(path), candidate.toLowerCase());
    }

    public static Predicate equalsLowerCaseBothPredicate(
        Expression<String> path, CriteriaBuilder cb, String candidate) {
        return cb.equal(cb.lower(path), candidate.toLowerCase());
    }

    public static Predicate likeLowerCaseStartsWithPredicate(
        Expression<String> path, CriteriaBuilder cb, String candidate) {
        return likeLowerCasePredicate(path, cb, candidate + "%");
    }

    public static Predicate likeLowerCaseEndsWithPredicate(
        Expression<String> path, CriteriaBuilder cb, String candidate) {
        return likeLowerCasePredicate(path, cb, "%" + candidate);
    }

    public static Predicate likeLowerCasePredicate(Expression<String> path, CriteriaBuilder cb, String candidate) {
        return cb.like(cb.lower(path), candidate);
    }

    public static Predicate equalsLowerCasePredicate(Expression<String> path, CriteriaBuilder cb, String candidate) {
        return cb.equal(cb.lower(path), candidate);
    }

    public static Optional<LocalDateTime> notNullOffsetDateTime(OffsetDateTime value) {
        return Optional.ofNullable(value).map(OffsetDateTime::toLocalDateTime);
    }

    public static Predicate andAll(CriteriaBuilder cb, List<Predicate> predicates) {
        predicates.removeIf(Objects::isNull);
        return predicates.isEmpty() ? null : cb.and(predicates.toArray(Predicate[]::new));
    }

    public static Predicate orAll(CriteriaBuilder cb, List<Predicate> predicates) {
        predicates.removeIf(Objects::isNull);
        return predicates.isEmpty() ? null : cb.or(predicates.toArray(Predicate[]::new));
    }
}
