package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessage;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessageGroup;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessageType;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsProcessedFileSummaryResponse;

class InterfaceJobsProcessedFileSummaryResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesExpectedResponseFields() {
        // Arrange
        InterfaceJobsMessage message = new InterfaceJobsMessage()
            .interfaceMessagesId(1L)
            .messageData(Map.of("number", 1))
            .messageType(InterfaceJobsMessageType.ERROR);

        InterfaceJobsMessageGroup messageGroup = new InterfaceJobsMessageGroup()
            .messageText("records_rejected")
            .messages(List.of(message));

        InterfaceJobsProcessedFileSummaryResponse response = InterfaceJobsProcessedFileSummaryResponse.builder()
            .fileName("auto-payments-in.dat")
            .source("NATWEST")
            .businessUnitName("Luton")
            .totalAmount(new BigDecimal("12.34"))
            .totalRecords((short) 2)
            .totalErrors(1L)
            .interfaceMessages(List.of(messageGroup))
            .build();

        // Act
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        // Assert
        assertEquals(Set.of("file_name", "source", "business_unit_name", "total_amount", "total_records",
            "total_errors", "interface_messages"), fieldNames(json));
        assertEquals(Set.of("message_text", "messages"), fieldNames(json.get("interface_messages").get(0)));
        assertEquals(Set.of("interface_messages_id", "message_data", "message_type"),
            fieldNames(json.get("interface_messages").get(0).get("messages").get(0)));
        assertEquals("Error", json.get("interface_messages").get(0).get("messages").get(0)
            .get("message_type").asString());
        assertEquals("12.34", json.get("total_amount").asString());
    }

    private Set<String> fieldNames(JsonNode node) {
        return node.properties().stream()
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }
}
