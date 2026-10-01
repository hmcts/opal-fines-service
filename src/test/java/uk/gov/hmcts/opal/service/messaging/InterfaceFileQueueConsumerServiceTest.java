package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.service.InterfaceFileProcessorService;

@ExtendWith(MockitoExtension.class)
class InterfaceFileQueueConsumerServiceTest {

    @Mock
    InterfaceFileProcessorService interfaceFileProcessorService;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    InterfaceFileQueueConsumerService consumer;

    @Test
    void consume_validPayload_parsesMessageAndSendsToService() throws Exception {

        String messagePayload = "{\"interfaceFileId\":1}";
        InterfaceFileQueueMessage message = new InterfaceFileQueueMessage(1L);

        when(objectMapper.readValue(
            messagePayload,
            InterfaceFileQueueMessage.class
        )).thenReturn(message);

        consumer.consume(messagePayload);

        verify(interfaceFileProcessorService, times(1))
            .process(message.interfaceFileId());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"     "})
    void consume_blankPayload_throwsIllegalArgumentException(String payload) {

        assertThatThrownBy(() -> consumer.consume(payload))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("payload is blank");

        verify(interfaceFileProcessorService, never())
            .process(any());
    }

    @Test
    void consume_invalidJson_throwsIllegalArgumentException() throws Exception {

        String payload = "{invalid";

        JacksonException parseException = new JacksonException("bad payload") {
        };

        when(objectMapper.readValue(
            payload,
            InterfaceFileQueueMessage.class
        )).thenThrow(parseException);

        assertThatThrownBy(() -> consumer.consume(payload))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unable to parse")
            .hasCause(parseException);

        verify(interfaceFileProcessorService, never())
            .process(any());
    }
}
