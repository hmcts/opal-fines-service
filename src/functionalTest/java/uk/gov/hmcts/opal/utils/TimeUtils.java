package uk.gov.hmcts.opal.utils;

import org.joda.time.LocalDateTime;

public final class TimeUtils {
    private TimeUtils() {}

    public static LocalDateTime secondsAgo(int seconds) {
        return LocalDateTime.now().minusSeconds(seconds);
    }
}
