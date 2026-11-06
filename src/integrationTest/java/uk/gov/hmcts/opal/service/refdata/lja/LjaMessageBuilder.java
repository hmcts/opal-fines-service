package uk.gov.hmcts.opal.service.refdata.lja;

import java.util.List;
import java.util.UUID;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaType;

public final class LjaMessageBuilder {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DEFAULT_START_DATE = "2026-01-01";

    private LjaMessageBuilder() {
    }

    public static String buildRefDataLjaMessage(String dataProduct, int ignoredRecordCount, boolean includeLjaName,
        String ljaCode, String ljaName, String endDate, String addressLine1, String addressLine2,
        String addressLine3, String addressLine4, String addressLine5, String postcode) {
        return buildMessage(dataProduct, List.of(buildRecord(ljaCode, includeLjaName ? ljaName : null, "CRWCRT",
            "2027-03-01", endDate, List.of(
                address("Court Address", addressLine1, addressLine2, addressLine3, addressLine4, addressLine5, postcode),
                address("Financial Office Address", "Secondary address line 1", null, null, null, null,
                    "NE1 2BB")))));
    }

    public static String validMessage(short id, String type) {
        return buildMessage("LJA", List.of(buildRecord("%04d".formatted(id), "Test LJA", type,
            DEFAULT_START_DATE, null, List.of(address("Court Address", "Test address", null, null, null, null,
                "GIR 0AA")))));
    }

    public static String validUnsupportedReferenceDataMessage() {
        ObjectNode record = OBJECT_MAPPER.createObjectNode();
        record.put("court_name", "Ignored Court");
        record.put("start_date", DEFAULT_START_DATE);
        record.put("publishing_status", "Active");
        record.put("cja_code", "41");
        record.put("court_location_code", "B01AA00");
        ObjectNode address = record.putArray("addresses").addObject();
        address.put("address_type", "Court Address");
        address.put("address_line_1", "Ignored address");
        address.put("postcode", "GIR 0AA");
        return buildMessage("Court", List.of(record));
    }

    public static String messageWithProcessingFailure(short firstId, short failingId) {
        return buildMessage("LJA", List.of(
            buildRecord("%04d".formatted(firstId), "First LJA", "CRWCRT", DEFAULT_START_DATE, null,
                List.of(address("Court Address", "First address", "First address line 2", "First address line 3",
                    "First address line 4", "First address line 5", "FI1 1AA"))),
            buildRecord("%04d".formatted(failingId), "Failing LJA", "CRWCRT", DEFAULT_START_DATE, null,
                List.of())));
    }

    public static String messageWithAddresses(short id) {
        return buildMessage("LJA", List.of(buildRecord("%04d".formatted(id), "Test LJA", "CRWCRT",
            DEFAULT_START_DATE, null, List.of(
                address("Financial Office Address", "Financial line 1", null, null, null, null, "GIR 0AA"),
                address("Court Address", "Court line 1", "Court line 2", "Court line 3", "Court line 4",
                    "Court line 5", "GIR 0AA")))));
    }

    public static String messageWithEndDate(short id, String endDate) {
        return buildMessage("LJA", List.of(buildRecord("%04d".formatted(id), "Test LJA", "CRWCRT",
            DEFAULT_START_DATE, endDate, List.of(address("Court Address", "Court line 1", null, null, null, null,
                "GIR 0AA")))));
    }

    public static LocalJusticeAreaEntity entity(short id) {
        LocalJusticeAreaEntity entity = new LocalJusticeAreaEntity();
        entity.setLocalJusticeAreaId(id);
        entity.setName("Test LJA");
        entity.setLjaType(LocalJusticeAreaType.CRWCRT);
        entity.setAddressLine1("Test address");
        entity.setPostcode("GIR 0AA");
        return entity;
    }

    private static String buildMessage(String dataProduct, List<ObjectNode> records) {
        try {
            ObjectNode root = OBJECT_MAPPER.createObjectNode();
            ObjectNode header = root.putObject("header");
            header.put("message_id", UUID.randomUUID().toString());
            header.put("message_type", "ReferenceData");
            header.put("data_product", dataProduct);
            header.put("operation", "PUBLISH");
            header.put("source_system", "Semarchy");
            header.put("created_date_time", "2026-01-01T00:00:00Z");
            header.put("release_package_id", 1);
            header.put("record_count", records.size());
            ArrayNode recordsNode = root.putObject("payload").putArray("records");
            records.forEach(recordsNode::add);
            return OBJECT_MAPPER.writeValueAsString(root);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to build ref-data test message", exception);
        }
    }

    private static ObjectNode buildRecord(String code, String name, String type, String startDate, String endDate,
        List<Address> addresses) {
        ObjectNode record = OBJECT_MAPPER.createObjectNode();
        record.put("lja_code", code);
        if (name != null) {
            record.put("lja_name", name);
        }
        record.put("lja_type", type);
        record.put("start_date", startDate);
        if (endDate == null) {
            record.putNull("end_date");
        } else {
            record.put("end_date", endDate);
        }
        record.put("publishing_status", "Active");
        record.put("cja_code", "41");
        ArrayNode addressNodes = record.putArray("addresses");
        addresses.forEach(address -> addAddress(addressNodes, address));
        record.putArray("contact_information");
        return record;
    }

    private static void addAddress(ArrayNode addresses, Address address) {
        ObjectNode node = addresses.addObject();
        node.put("address_type", address.type());
        putIfPresent(node, "address_line_1", address.line1());
        putIfPresent(node, "address_line_2", address.line2());
        putIfPresent(node, "address_line_3", address.line3());
        putIfPresent(node, "address_line_4", address.line4());
        putIfPresent(node, "address_line_5", address.line5());
        putIfPresent(node, "postcode", address.postcode());
    }

    private static void putIfPresent(ObjectNode node, String name, String value) {
        if (value != null) {
            node.put(name, value);
        }
    }

    private static Address address(String type, String line1, String line2, String line3, String line4, String line5,
        String postcode) {
        return new Address(type, line1, line2, line3, line4, line5, postcode);
    }

    private record Address(String type, String line1, String line2, String line3, String line4, String line5,
                           String postcode) { }
}
