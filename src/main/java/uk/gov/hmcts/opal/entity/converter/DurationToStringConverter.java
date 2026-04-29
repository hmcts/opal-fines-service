package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Duration;
import java.time.Period;

/**
 * JPA AttributeConverter that maps between a Java {@link Duration} and an ISO-8601 string stored as VARCHAR in the
 * database (e.g. "P14D", "P30D", "PT5H").
 *
 * <p>Supports both Period format (e.g. "P14D") and Duration format (e.g. "PT5H30M").
 *
 * @author Krishna Sapkota
 */
@Converter
public class DurationToStringConverter implements AttributeConverter<Duration, String> {

    @Override
    public String convertToDatabaseColumn(Duration duration) {
        if (duration == null) {
            return null;
        }
        // Store as Period format (P{n}D) if it represents whole days, otherwise ISO-8601 Duration
        long days = duration.toDays();
        if (duration.equals(Duration.ofDays(days))) {
            return Period.ofDays((int) days).toString(); // e.g. "P14D"
        }
        return duration.toString(); // e.g. "PT5H30M"
    }

    @Override
    public Duration convertToEntityAttribute(String dbValue) {
        if (dbValue == null || dbValue.isBlank()) {
            return null;
        }
        // Period format e.g. "P14D" — contains no 'T' after 'P'
        if (!dbValue.contains("T")) {
            Period period = Period.parse(dbValue);
            return Duration.ofDays(period.getDays() + (period.getMonths() * 30L) + (period.getYears() * 365L));
        }
        return Duration.parse(dbValue); // e.g. "PT5H30M"
    }
}

