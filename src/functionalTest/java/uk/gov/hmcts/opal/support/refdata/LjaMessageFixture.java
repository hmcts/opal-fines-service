package uk.gov.hmcts.opal.support.refdata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Builds the publisher wire contract, including deliberately invalid field values. */
public final class LjaMessageFixture {
    private static final String RESOURCE = "/refdata/lja/publication.json";

    private LjaMessageFixture() {
    }

    public record AddressData(String type, List<String> lines, String postcode) {
    }

    public record RecordData(String code, String name, String type, LocalDate startDate, LocalDate endDate,
                             String publishingStatus, List<AddressData> addresses) {
    }

    public record Message(UUID id, Map<String, Object> body) {
    }

    @SuppressWarnings("unchecked")
    public static Message create(List<RecordData> records) {
        Map<String, Object> body = loadFixture();
        UUID id = UUID.randomUUID();
        var header = (Map<String, Object>) body.get("header");
        header.put("message_id", id.toString());
        header.put("created_date_time", Instant.now().toString());
        header.put("record_count", records.size());
        var payload = (Map<String, Object>) body.get("payload");
        Map<String, Object> template = templateRecord(body);
        payload.put("records", records.stream().map(record -> wireRecord(record, template)).toList());
        return new Message(id, body);
    }

    @SuppressWarnings("unchecked")
    public static RecordData fromFixture(short id, String name) {
        Map<String, Object> template = templateRecord(loadFixture());
        List<AddressData> addresses = new ArrayList<>();
        for (var address : (List<Map<String, Object>>) template.get("addresses")) {
            List<String> lines = new ArrayList<>();
            for (int index = 1; index <= 5; index++) {
                lines.add((String) address.get("address_line_" + index));
            }
            addresses.add(new AddressData((String) address.get("address_type"), lines,
                (String) address.get("postcode")));
        }
        return new RecordData(String.format("%04d", id), name, (String) template.get("lja_type"),
            date((String) template.get("start_date")), date((String) template.get("end_date")),
            (String) template.get("publishing_status"), addresses);
    }

    public static RecordData withOverrides(RecordData baseline, Map<String, String> overrides) {
        for (String field : overrides.keySet()) {
            if (!List.of("lja_name", "end_date", "address_line_1").contains(field)) {
                throw new IllegalArgumentException("Unsupported LJA override: " + field);
            }
        }
        List<AddressData> addresses = new ArrayList<>();
        for (AddressData address : baseline.addresses()) {
            List<String> lines = new ArrayList<>(address.lines());
            if (address.type().equals("Court Address") && overrides.containsKey("address_line_1")) {
                lines.set(0, value(overrides, "address_line_1", null));
            }
            addresses.add(new AddressData(address.type(), lines, address.postcode()));
        }
        return new RecordData(baseline.code(), value(overrides, "lja_name", baseline.name()), baseline.type(),
            baseline.startDate(), overrides.containsKey("end_date") ? date(value(overrides, "end_date", null))
                : baseline.endDate(), baseline.publishingStatus(), addresses);
    }

    private static Map<String, Object> loadFixture() {
        try (var input = LjaMessageFixture.class.getResourceAsStream(RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("Missing LJA publication JSON template");
            }
            return new ObjectMapper().readValue(input, new TypeReference<>() { });
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load LJA publication JSON template", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> templateRecord(Map<String, Object> body) {
        var payload = (Map<String, Object>) body.get("payload");
        return (Map<String, Object>) ((List<?>) payload.get("records")).getFirst();
    }

    private static String value(Map<String, String> row, String field, String fallback) {
        String value = row.getOrDefault(field, fallback);
        if ("<null>".equals(value)) {
            return null;
        }
        return value;
    }

    private static LocalDate date(String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    private static Map<String, Object> wireRecord(RecordData record, Map<String, Object> template) {
        Map<String, Object> result = new LinkedHashMap<>(template);
        result.put("lja_code", record.code());
        result.put("lja_name", record.name());
        result.put("lja_type", record.type());
        result.put("start_date", record.startDate().toString());
        result.put("end_date", record.endDate() == null ? null : record.endDate().toString());
        result.put("publishing_status", record.publishingStatus());
        result.put("addresses", record.addresses().stream().map(address -> {
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("address_type", address.type());
            for (int index = 0; index < 5; index++) {
                fields.put("address_line_" + (index + 1), address.lines().get(index));
            }
            fields.put("postcode", address.postcode());
            return fields;
        }).toList());
        return result;
    }
}
