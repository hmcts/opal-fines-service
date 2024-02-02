package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.DateDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class EntitySpecs<E> {

    @SafeVarargs
    public final List<Specification<E>> specificationList(
        Optional<Specification<E>>... optionalSpecs) {
        return Arrays.stream(optionalSpecs)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    public Optional<String> notBlank(String candidate) {
        return Optional.ofNullable(candidate).filter(s -> !s.isBlank());
    }

    public Optional<LocalDate> notNullLocalDate(DateDto candidate) {
        return Optional.ofNullable(candidate).map(DateDto::toLocalDate);
    }

    public Optional<LocalDateTime> notNullLocalDateTime(DateDto candidate) {
        return Optional.ofNullable(candidate).map(DateDto::toLocalDate).map(d -> d.atTime(0, 0, 0));
    }
}
