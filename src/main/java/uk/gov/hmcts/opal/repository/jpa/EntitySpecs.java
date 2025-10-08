package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.DateDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class EntitySpecs<E> {

    private static final String SQL_REPLACE = "REPLACE";

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

    public static Predicate likeStartsWithPredicate(Path<String> path, CriteriaBuilder builder, String candidate) {
        return likeLowerCasePredicate(path, builder, candidate + "%");
    }

    public static Predicate likeWildcardPredicate(Path<String> path, CriteriaBuilder builder, String candidate) {
        return likeLowerCasePredicate(path, builder, "%" + candidate + "%");
    }

    public static Predicate likeLowerCasePredicate(Path<String> path, CriteriaBuilder builder, String candidate) {
        return builder.like(builder.lower(path), candidate.toLowerCase());
    }

    public static Optional<LocalDateTime> notNullOffsetDateTime(OffsetDateTime value) {
        return Optional.ofNullable(value).map(OffsetDateTime::toLocalDateTime);
    }

    /* ===== Shared normalization helpers (accessible to subclasses) ===== */
    protected static Expression<String> normalized(CriteriaBuilder cb, Expression<String> x) {
        Expression<String> noSpaces = cb.function(SQL_REPLACE, String.class, x, cb.literal(" "), cb.literal(""));
        Expression<String> noHyphens =
            cb.function(SQL_REPLACE, String.class, noSpaces, cb.literal("-"), cb.literal(""));
        Expression<String> noApos = cb.function(SQL_REPLACE, String.class, noHyphens, cb.literal("'"), cb.literal(""));
        return cb.lower(noApos);
    }

    protected static String normalizeLiteral(String s) {
        return s == null ? null : s.toLowerCase().replace(" ", "").replace("-", "").replace("'", "");
    }

    protected static Predicate likeStartsWithNormalized(CriteriaBuilder cb, Expression<String> field, String value) {
        return cb.like(normalized(cb, field), normalizeLiteral(value) + "%");
    }

    protected static Predicate equalsNormalized(CriteriaBuilder cb, Expression<String> field, String value) {
        return cb.equal(normalized(cb, field), normalizeLiteral(value));
    }

    protected static String stripCheckLetter(String acc) {
        if (acc == null) {
            return null;
        }
        return (acc.length() == 9 && Character.isLetter(acc.charAt(8)))
            ? acc.substring(0, 8)
            : acc;
    }

}
