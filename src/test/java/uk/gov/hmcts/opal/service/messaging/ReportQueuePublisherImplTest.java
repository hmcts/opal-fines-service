package uk.gov.hmcts.opal.service.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.config.ReportServiceBusProperties;

@ExtendWith(MockitoExtension.class)
class ReportQueuePublisherImplTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ReportServiceBusProperties properties;

    @InjectMocks
    private ReportQueuePublisherImpl reportQueuePublisher;

    @Test
    void test_addReportInstanceToQueue() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("PAYLOAD");
        when(properties.getQueueName()).thenReturn("Queue name");

        reportQueuePublisher.publish(1L);
        verify(jmsTemplate).convertAndSend("Queue name", "PAYLOAD");
    }
}
