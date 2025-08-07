package uk.gov.hmcts.opal.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DateTimeUtils {

    public static OffsetDateTime toUtcDateTime(LocalDateTime localDateTime) {
        return Optional.ofNullable(localDateTime)
            .map(ldt -> ldt.atOffset(ZoneOffset.UTC))
            .orElse(null);
    }

    public static String toString(LocalDate localDate) {
        return Optional.ofNullable(localDate)
            .map(DateTimeFormatter.ISO_DATE::format)
            .orElse(null);
    }
}
