package uk.gov.hmcts.opal.service.opal.history.defendant.sources;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.data.jpa.domain.Specification;

abstract class HistorySourceSpecificationSupport {

    protected LocalDateTime atStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    protected LocalDateTime dayAfterStart(LocalDate date) {
        return date.plusDays(1).atStartOfDay();
    }

    @SafeVarargs
    protected final <T> Specification<T> allOf(Specification<T>... specifications) {
        return Specification.allOf(Stream.of(specifications)
            .filter(Objects::nonNull)
            .toList());
    }
}
