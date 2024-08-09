package uk.gov.hmcts.opal.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

public class DateTimeUtils {

    public static OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
        return Optional.ofNullable(localDateTime)
            .map(ldt -> ldt.atOffset(ZoneOffset.UTC))
            .orElse(null);
    }
}
