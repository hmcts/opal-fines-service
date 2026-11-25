package uk.gov.hmcts.opal.service.refdata.prison;

import java.util.List;
import java.util.UUID;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

final class PrisonMessageBuilder {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private PrisonMessageBuilder() {
    }

    static String message(String name, String addressType, String addressLine1, String postcode) {
        return buildMessage(List.of(record(name, addressType, addressLine1, postcode)));
    }

    static String messageWithAddresses(String name) {
        ObjectNode record = record(name, "PRIS", "Prison line 1", "PR1 1AA");
        ArrayNode addresses = record.withArray("addresses");
        addresses.insert(0, address("OTHER", "Ignored line", "ZZ1 1ZZ"));
        return buildMessage(List.of(record));
    }

    static String messageWithProcessingFailure(String validName, String invalidName) {
        return buildMessage(List.of(
            record(validName, "PRIS", "First prison line", "PR1 1AA"),
            record(invalidName, "PRIS", "Failing prison line", "PR2 2BB")));
    }

    static String validMessage(String name) {
        return message(name, "PRIS", "Test prison line", "PR3 3CC");
    }

    private static ObjectNode record(String name, String addressType, String addressLine1, String postcode) {
        ObjectNode record = OBJECT_MAPPER.createObjectNode();
        record.put("name", name);
        record.put("start_date", "2026-01-01");
        record.put("publishing_status", "Active");
        ArrayNode addresses = record.putArray("addresses");
        addresses.add(address(addressType, addressLine1, postcode));
        record.putArray("contact_information");
        return record;
    }

    private static ObjectNode address(String type, String line1, String postcode) {
        ObjectNode address = OBJECT_MAPPER.createObjectNode();
        address.put("address_type", type);
        address.put("address_line_1", line1);
        address.put("postcode", postcode);
        return address;
    }

    private static String buildMessage(List<ObjectNode> records) {
        try {
            ObjectNode root = OBJECT_MAPPER.createObjectNode();
            ObjectNode header = root.putObject("header");
            header.put("message_id", UUID.randomUUID().toString());
            header.put("message_type", "ReferenceData");
            header.put("data_product", "Prison");
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
}
