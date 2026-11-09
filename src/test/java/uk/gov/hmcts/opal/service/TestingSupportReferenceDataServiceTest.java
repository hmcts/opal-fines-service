package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.config.TestingSupportReferenceDataPublisherConfig.REF_DATA_SESSION_ID;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.exception.InvalidReferenceValidationException;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.refdata.RefDataHandlerRegistry;
import uk.gov.hmcts.opal.refdata.RefDataUpdateHandler;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@ExtendWith(MockitoExtension.class)
class TestingSupportReferenceDataServiceTest {

    private static final String REF_DATA_SCHEMA = "ref-data/ref_data_schema.json";

    @Mock
    private RefDataHandlerRegistry handlerRegistry;
    @Mock
    private RefDataUpdateHandler<Object, Object> handler;
    @Mock
    private JsonSchemaValidationService jsonSchemaValidationService;
    @Mock
    private ServiceBusSenderClient senderClient;

    private ObjectMapper objectMapper;
    private TestingSupportReferenceDataService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new TestingSupportReferenceDataService(handlerRegistry, objectMapper,
            jsonSchemaValidationService, senderClient);
    }

    @Test
    void deleteReferenceData_usesMatchingHandler() {
        when(handlerRegistry.find("Court")).thenReturn(Optional.of(handler));

        service.deleteReferenceData("Court", "123");

        verify(handler).deleteEntity("123");
    }

    @Test
    void deleteReferenceData_rejectsUnsupportedType() {
        when(handlerRegistry.find("Prison")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteReferenceData("Prison", "123"))
            .isInstanceOf(InvalidReferenceValidationException.class)
            .hasMessage("Unsupported reference data type: Prison");

        verifyNoInteractions(handler);
    }

    @Test
    void deleteReferenceData_translatesInvalidBusinessKey() {
        IllegalArgumentException failure = new IllegalArgumentException("Invalid business key received: invalid");
        when(handlerRegistry.find("LJA")).thenReturn(Optional.of(handler));
        doThrow(failure).when(handler).deleteEntity("invalid");

        assertThatThrownBy(() -> service.deleteReferenceData("LJA", "invalid"))
            .isInstanceOf(InvalidReferenceValidationException.class)
            .hasMessage("Invalid business key received: invalid")
            .hasCause(failure);
    }

    @Test
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
        assertThat(message.getSessionId()).isEqualTo(REF_DATA_SESSION_ID);
    }

    @Test
    void publishReferenceData_doesNotSendInvalidMessage() {
        Map<String, Object> requestBody = Map.of("invalid", true);
        JsonSchemaValidationException failure = new JsonSchemaValidationException("Invalid reference data message");
        doThrow(failure).when(jsonSchemaValidationService).validateOrError(anyString(), eq(REF_DATA_SCHEMA));

        assertThatThrownBy(() -> service.publishReferenceData(requestBody)).isSameAs(failure);

        verifyNoInteractions(senderClient);
    }
}
