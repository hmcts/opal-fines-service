package uk.gov.hmcts.opal.util;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

    private static final DateTimeFormatter LEGACY_DATE_TIME =
        new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
            .optionalEnd()
            .toFormatter();

    @Override
    public LocalDateTime unmarshal(String v) throws Exception {
        if (v == null || v.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(v, LEGACY_DATE_TIME);
        } catch (DateTimeParseException e) {
            return LocalDate.parse(v).atStartOfDay();
        }
    }

    @Override
    public String marshal(LocalDateTime v) throws Exception {
        return v.toString();
    }
}
