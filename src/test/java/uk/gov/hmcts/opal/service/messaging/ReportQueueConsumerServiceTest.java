package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.service.report.GenericReportService;

@ExtendWith(MockitoExtension.class)
class ReportQueueConsumerServiceTest {

    @Mock
    GenericReportService genericReportService;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    ReportQueueConsumerService consumer;

    @Test
    void consume_validPayload_parsesMessageAndSendsToService() throws Exception {
        String messagePayload = "{\"instance_id\":1}";
        ReportQueueMessage message = new ReportQueueMessage(1L);
        when(objectMapper.readValue(messagePayload, ReportQueueMessage.class)).thenReturn(message);
        consumer.consume(messagePayload);
        verify(genericReportService, times(1))
            .generateReportInstanceContent(message.instanceId());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"     "})
    void consume_blankPayload_throwsIllegalArgumentException(String payload) {
        assertThatThrownBy(() -> consumer.consume(payload))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("payload is blank");
        verify(genericReportService, never()).generateReportInstanceContent(Mockito.any());
    }

    @Test
    void consume_invalidJson_throwsIllegalArgumentException() throws JacksonException {
        String payload = "{invalid";
        JacksonException parseException = new JacksonException("bad payload") {
        };
        when(objectMapper.readValue(payload, ReportQueueMessage.class)).thenThrow(parseException);
        assertThatThrownBy(() -> consumer.consume(payload))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unable to parse")
            .hasCause(parseException);
        verify(genericReportService, never()).generateReportInstanceContent(Mockito.any());
    }
}
