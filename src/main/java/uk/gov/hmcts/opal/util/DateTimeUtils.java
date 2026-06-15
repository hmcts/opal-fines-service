package uk.gov.hmcts.opal.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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

    public static LocalDateTime startOf(LocalDate localDate) {
        return localDate == null ? null : localDate.atStartOfDay();
    }

    public static LocalDateTime endOf(LocalDate localDate) {
        return localDate == null ? null : localDate.atTime(LocalTime.MAX);
    }

    public static LocalDate todayUk() {
        return LocalDate.now(ZoneId.of("Europe/London"));
    }

    public static LocalDate todayPlusDaysUk(int days) {
        return todayUk().plusDays(days);
    }

}
