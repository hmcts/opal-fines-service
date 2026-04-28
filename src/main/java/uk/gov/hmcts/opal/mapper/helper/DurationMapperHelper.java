package uk.gov.hmcts.opal.mapper.helper;

import java.time.Duration;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

/**
 * Helper class for mapping Duration objects in MapStruct mappers. Provides reusable conversion methods between Duration
 * and String representations.
 */
@Component
public class DurationMapperHelper {

    /**
     * Converts a Duration to ISO-8601 string format. For whole days, returns Period format (e.g., "P30D"). For other
     * durations, returns standard Duration format (e.g., "PT5H30M").
     *
     * @param duration the Duration to convert
     * @return ISO-8601 formatted string, or null if duration is null
     */
    @Named("durationToString")
    public String durationToString(Duration duration) {
        if (duration == null) {
            return null;
        }
        // Convert Duration to Period format (P{n}D) if it's a whole number of days
        long days = duration.toDays();
        if (duration.equals(Duration.ofDays(days))) {
            return "P" + days + "D";
        }
        // Otherwise use standard Duration ISO-8601 format
        return duration.toString();
    }

    /**
     * Converts an ISO-8601 string to Duration. Supports both Period format (e.g., "P30D") and Duration format (e.g.,
     * "PT5H").
     *
     * @param durationString the ISO-8601 formatted string
     * @return Duration object, or null if string is null or empty
     */
    @Named("stringToDuration")
    public Duration stringToDuration(String durationString) {
        if (durationString == null || durationString.trim().isEmpty()) {
            return null;
        }

        try {
            // Handle Period format (e.g., "P30D") by converting to Duration
            if (durationString.matches("^P\\d+D$")) {
                long days = Long.parseLong(durationString.substring(1, durationString.length() - 1));
                return Duration.ofDays(days);
            }
            // Handle standard Duration format (e.g., "PT5H30M")
            return Duration.parse(durationString);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid duration format: " + durationString, e);
        }
    }
}