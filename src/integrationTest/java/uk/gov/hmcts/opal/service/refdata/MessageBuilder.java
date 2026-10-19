package uk.gov.hmcts.opal.service.refdata;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

public class MessageBuilder {

    public static String buildRefDataLjaMessage(String dataProduct, int recordCount, boolean includeLjaName, String ljaCode,
        String ljaName, String endDate, String addressLine1, String addressLine2, String addressLine3,
        String addressLine4, String postcode) {
        ObjectMapper objectMapper = new ObjectMapper();
        return buildRefDataLjaMessage(
            objectMapper,
            dataProduct,
            recordCount,
            buildLjaRecordNode(objectMapper, includeLjaName, ljaCode, ljaName, endDate, addressLine1, addressLine2,
                addressLine3, addressLine4, postcode)
        );
    }

    private static String buildRefDataLjaMessage(ObjectMapper objectMapper, String dataProduct, int recordCount,
        ObjectNode... recordNodes) {
        try {
            ObjectNode rootNode = objectMapper.createObjectNode();
            ObjectNode headerNode = rootNode.putObject("header");
            headerNode.put("message_id", "437dacf6-511c-4e93-95f3-23e82b12e735");
            headerNode.put("message_type", "ReferenceData");
            headerNode.put("data_product", dataProduct);
            headerNode.put("operation", "PUBLISH");
            headerNode.put("source_system", "Semarchy");
            headerNode.put("created_date_time", "2026-09-02T08:28:56.935738+00:00");
            headerNode.put("release_package_id", 202);
            headerNode.put("record_count", recordCount);

            ObjectNode payloadNode = rootNode.putObject("payload");
            ArrayNode recordsNode = payloadNode.putArray("records");
            for (ObjectNode recordNode : recordNodes) {
                recordsNode.add(recordNode);
            }

            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to build ref-data test message", ex);
        }
    }

    private static ObjectNode buildLjaRecordNode(ObjectMapper objectMapper, boolean includeLjaName, String ljaCode,
        String ljaName, String endDate, String addressLine1, String addressLine2, String addressLine3,
        String addressLine4, String postcode) {
        ObjectNode recordNode = objectMapper.createObjectNode();
        recordNode.put("lja_code", ljaCode);
        if (includeLjaName) {
            recordNode.put("lja_name", ljaName);
        }
        recordNode.put("end_date", endDate);
        recordNode.put("lja_type", "CRWCRT");
        recordNode.put("start_date", "2027-03-01");
        recordNode.put("publishing_status", "Active");
        recordNode.put("cja_code", "41");


        ArrayNode addressesNode = recordNode.putArray("addresses");
        ObjectNode addressNode = addressesNode.addObject();
        addressNode.put("address_type", "Court Address");
        addressNode.put("address_line_1", addressLine1);
        addressNode.put("address_line_2", addressLine2);
        addressNode.put("address_line_3", addressLine3);
        addressNode.put("address_line_4", addressLine4);
        addressNode.put("post_code", postcode);

        ObjectNode secondaryAddressNode = addressesNode.addObject();
        secondaryAddressNode.put("address_type", "Financial Office Address");
        secondaryAddressNode.put("address_line_1", "Secondary address line 1");
        secondaryAddressNode.put("post_code", "NE1 2BB");

        recordNode.putArray("contact_information");

        return recordNode;
    }

}
