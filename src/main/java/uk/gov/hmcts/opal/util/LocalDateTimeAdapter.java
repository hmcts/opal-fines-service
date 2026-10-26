package uk.gov.hmcts.opal.util;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;

public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

    private static final DateTimeFormatter DATE_OR_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
        .append(DateTimeFormatter.ISO_LOCAL_DATE)
        .optionalStart()
        .appendLiteral('T')
        .append(DateTimeFormatter.ISO_LOCAL_TIME)
        .optionalEnd()
        .toFormatter();

    @Override
    public LocalDateTime unmarshal(String v) throws Exception {
        if (v == null || v.isBlank()) {
            return null;
        }

        String normalizedValue = v.trim().replace(' ', 'T');

        TemporalAccessor parsedValue = DATE_OR_DATE_TIME_FORMATTER
            .parseBest(normalizedValue, LocalDateTime::from, LocalDate::from);

        return parsedValue instanceof LocalDateTime dateTime ? dateTime : ((LocalDate) parsedValue).atStartOfDay();
    }

    @Override
    public String marshal(LocalDateTime v) throws Exception {
        return v.toString();
    }
}
