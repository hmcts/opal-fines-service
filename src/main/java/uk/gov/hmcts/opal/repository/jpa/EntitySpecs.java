package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.DateDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    public Optional<Short> numericShort(String candidate) {
        return numeric(candidate).map(Short::parseShort);
    }

    public Optional<LocalDate> notNullLocalDate(DateDto candidate) {
        return Optional.ofNullable(candidate).map(DateDto::toLocalDate);
    }

    public Optional<LocalDateTime> notNullLocalDateTime(DateDto candidate) {
        return Optional.ofNullable(candidate).map(DateDto::toLocalDate).map(d -> d.atTime(0, 0, 0));
    }

    public static Predicate likeWildcardPredicate(Path<String> path, CriteriaBuilder builder, String candidate) {
        return likeLowerCasePredicate(path, builder, "%" + candidate + "%");
    }

    public static Predicate likeLowerCasePredicate(Path<String> path, CriteriaBuilder builder, String candidate) {
        return builder.like(builder.lower(path),candidate.toLowerCase());
    }
}
