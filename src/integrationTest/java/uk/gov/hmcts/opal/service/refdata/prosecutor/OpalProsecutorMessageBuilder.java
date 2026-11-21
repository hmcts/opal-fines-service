package uk.gov.hmcts.opal.service.refdata.prosecutor;

import java.util.List;
import java.util.UUID;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

final class OpalProsecutorMessageBuilder {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DEFAULT_START_DATE = "2026-01-01";

    private OpalProsecutorMessageBuilder() {
    }

    static String messageWithAddresses(int prosecutorCode) {
        return buildMessage(List.of(buildRecord(prosecutorCode, "Test Prosecutor", null, List.of(
            address("Court Address", "Court line 1", null, null, null, null, "GIR 0AA"),
            address("Prosecutor", "Prosecutor line 1", "Prosecutor line 2", "Prosecutor line 3",
                "Prosecutor line 4", "Prosecutor line 5", "PR1 1AA")))));
    }

    static String messageWithEndDateAndNoAddresses(int prosecutorCode, String endDate) {
        return buildMessage(List.of(buildRecord(prosecutorCode, "Updated Prosecutor", endDate, null)));
    }

    static String messageWithProcessingFailure(int validCode, int failingCode) {
        return buildMessage(List.of(
            buildRecord(validCode, "Valid Prosecutor", null,
                List.of(address("Prosecutor", "Valid address", null, null, null, null, "PR1 1AA"))),
            buildRecord(failingCode, "Failing Prosecutor", null, List.of())));
    }

    static String validMessage(int prosecutorCode) {
        return buildMessage(List.of(buildRecord(prosecutorCode, "Test Prosecutor", null,
            List.of(address("Prosecutor", "Prosecutor address", null, null, null, null, "PR1 1AA")))));
    }

    private static String buildMessage(List<ObjectNode> records) {
        try {
            ObjectNode root = OBJECT_MAPPER.createObjectNode();
            ObjectNode header = root.putObject("header");
            header.put("message_id", UUID.randomUUID().toString());
            header.put("message_type", "ReferenceData");
            header.put("data_product", "Prosecutor");
            header.put("operation", "PUBLISH");
            header.put("source_system", "Semarchy");
            header.put("created_date_time", "2026-01-01T00:00:00Z");
            header.put("release_package_id", 1);
            header.put("record_count", records.size());
            ArrayNode recordsNode = root.putObject("payload").putArray("records");
            records.forEach(recordsNode::add);
            return OBJECT_MAPPER.writeValueAsString(root);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to build prosecutor ref-data test message", exception);
        }
    }

    private static ObjectNode buildRecord(int prosecutorCode, String name, String endDate,
        List<Address> addresses) {
        ObjectNode record = OBJECT_MAPPER.createObjectNode();
        record.put("prosecutor_code", prosecutorCode);
        record.put("name", name);
        record.put("short_name", "Test");
        record.put("start_date", DEFAULT_START_DATE);
        if (endDate == null) {
            record.putNull("end_date");
        } else {
            record.put("end_date", endDate);
        }
        record.put("publishing_status", "Active");
        if (addresses == null) {
            record.putNull("addresses");
        } else {
            ArrayNode addressNodes = record.putArray("addresses");
            addresses.forEach(address -> addAddress(addressNodes, address));
        }
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

    private static Address address(String type, String line1, String line2, String line3, String line4,
        String line5, String postcode) {
        return new Address(type, line1, line2, line3, line4, line5, postcode);
    }

    private record Address(String type, String line1, String line2, String line3, String line4, String line5,
                           String postcode) {
    }
}
