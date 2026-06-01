package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Duration;

@Converter
public class DurationToStringConverter implements AttributeConverter<Duration, String> {

    @Override
    public String convertToDatabaseColumn(Duration duration) {
        return duration == null ? null : duration.toString();
    }

    @Override
    public Duration convertToEntityAttribute(String value) {
        return value == null ? null : Duration.parse(value);
    }
}
