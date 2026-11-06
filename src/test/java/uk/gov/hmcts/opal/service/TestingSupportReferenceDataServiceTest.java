package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.gov.hmcts.opal.exception.InvalidReferenceValidationException;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.refdata.RefDataUpdateHandler;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@ExtendWith(MockitoExtension.class)
class TestingSupportReferenceDataServiceTest {

    private static final String REF_DATA_SCHEMA = "ref-data/ref_data_schema.json";

    @Mock
    private RefDataUpdateHandler<Object, Object> ljaHandler;
    @Mock
    private RefDataUpdateHandler<Object, Object> courtHandler;
    @Mock
    private JsonSchemaValidationService jsonSchemaValidationService;
    @Mock
    private ServiceBusSenderClient senderClient;

    private ObjectMapper objectMapper;
    private TestingSupportReferenceDataService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new TestingSupportReferenceDataService(List.of(ljaHandler, courtHandler), objectMapper,
            jsonSchemaValidationService, senderClient);
    }

    @Test
    @JiraStory("PO-10705")
    @JiraEpic("PO-9031")
    void deleteReferenceData_usesMatchingHandler() {
        when(ljaHandler.refDataType()).thenReturn("LJA");
        when(courtHandler.refDataType()).thenReturn("Court");

        service.deleteReferenceData("Court", "123");

        verify(courtHandler).deleteEntity("123");
        verify(ljaHandler, never()).deleteEntity(anyString());
    }

    @Test
    @JiraStory("PO-10705")
    @JiraEpic("PO-9031")
    void deleteReferenceData_rejectsUnsupportedType() {
        when(ljaHandler.refDataType()).thenReturn("LJA");
        when(courtHandler.refDataType()).thenReturn("Court");

        assertThatThrownBy(() -> service.deleteReferenceData("Prison", "123"))
            .isInstanceOf(InvalidReferenceValidationException.class)
            .hasMessage("Unsupported reference data type: Prison");

        verify(ljaHandler, never()).deleteEntity(anyString());
        verify(courtHandler, never()).deleteEntity(anyString());
    }

    @Test
    @JiraStory("PO-10705")
    @JiraEpic("PO-9031")
    void deleteReferenceData_translatesInvalidBusinessKey() {
        IllegalArgumentException failure = new IllegalArgumentException("Invalid business key received: invalid");
        when(ljaHandler.refDataType()).thenReturn("LJA");
        doThrow(failure).when(ljaHandler).deleteEntity("invalid");

        assertThatThrownBy(() -> service.deleteReferenceData("LJA", "invalid"))
            .isInstanceOf(InvalidReferenceValidationException.class)
            .hasMessage("Invalid business key received: invalid")
            .hasCause(failure);
    }

    @Test
    @JiraStory("PO-10705")
    @JiraEpic("PO-9031")
    void publishReferenceData_validatesBeforeSendingWithSessionId() throws Exception {
        Map<String, Object> requestBody = Map.of(
            "header", Map.of("data_product", "LJA"),
            "payload", Map.of("records", List.of(Map.of("lja_code", "0810"))));
        String expectedBody = objectMapper.valueToTree(requestBody).toString();

        service.publishReferenceData(requestBody);

        ArgumentCaptor<ServiceBusMessage> messageCaptor = ArgumentCaptor.forClass(ServiceBusMessage.class);
        InOrder validationBeforeSend = inOrder(jsonSchemaValidationService, senderClient);
        validationBeforeSend.verify(jsonSchemaValidationService).validateOrError(expectedBody, REF_DATA_SCHEMA);
        validationBeforeSend.verify(senderClient).sendMessage(messageCaptor.capture());

        ServiceBusMessage message = messageCaptor.getValue();
        assertThat(objectMapper.readTree(message.getBody().toString())).isEqualTo(objectMapper.readTree(expectedBody));
        assertThat(UUID.fromString(message.getSessionId())).isNotNull();
    }

    @Test
    @JiraStory("PO-10705")
    @JiraEpic("PO-9031")
    void publishReferenceData_doesNotSendInvalidMessage() {
        Map<String, Object> requestBody = Map.of("invalid", true);
        JsonSchemaValidationException failure = new JsonSchemaValidationException("Invalid reference data message");
        doThrow(failure).when(jsonSchemaValidationService).validateOrError(anyString(), eq(REF_DATA_SCHEMA));

        assertThatThrownBy(() -> service.publishReferenceData(requestBody)).isSameAs(failure);

        verifyNoInteractions(senderClient);
    }
}
