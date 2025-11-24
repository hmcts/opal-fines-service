package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalTime;

@Converter(autoApply = true)
public class LocalTimeStringAttributeConverter implements AttributeConverter<LocalTime, String> {

    @Override
    public String convertToDatabaseColumn(LocalTime time) {
        return time != null ? time.toString() : null;   // stored as "HH:mm"
    }

    @Override
    public LocalTime convertToEntityAttribute(String dbValue) {
        return dbValue != null ? LocalTime.parse(dbValue) : null;
    }
}
