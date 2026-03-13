package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.service.messaging.ReportQueueConsumerService;
import uk.gov.hmcts.opal.service.messaging.ReportQueueMessage;

@ExtendWith(MockitoExtension.class)
class ReportQueueConsumerServiceTest {

    @Mock
    GenericReportService genericReportService;
    static ReportQueueConsumerService consumer;

    private final ObjectMapper objectMapper = JsonMapper.builder()
        .findAndAddModules()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build();

    @BeforeEach
    public void setUp() {
        consumer = new ReportQueueConsumerService(objectMapper, genericReportService);
    }

    @Test
    void consume_validPayload_parsesMessageAndSendsToService() throws Exception {
        ReportQueueMessage details = new ReportQueueMessage(1L);
        String payload = objectMapper.writeValueAsString(details);
        consumer.consume(payload);
        verify(genericReportService, times(1))
            .generateReportInstanceContent(details.instanceId());
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
    void consume_invalidJson_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> consumer.consume("{invalid"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unable to parse");
        verify(genericReportService, never()).generateReportInstanceContent(Mockito.any());
    }
}