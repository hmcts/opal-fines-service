package uk.gov.hmcts.opal.service.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import uk.gov.hmcts.opal.config.ReportQueuePublisherProperties;

@ExtendWith(MockitoExtension.class)
public class ReportQueuePublisherImplTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ReportQueuePublisherProperties properties;

    @InjectMocks
    private ReportQueuePublisherImpl reportQueuePublisher;

    @Test
    public void test_addReportInstanceToQueue() throws JsonProcessingException {
        Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("PAYLOAD");
        Mockito.when(properties.getQueueName()).thenReturn("Queue name");

        reportQueuePublisher.publish(1L);
        Mockito.verify(jmsTemplate).convertAndSend(Mockito.eq("Queue name"), Mockito.eq("PAYLOAD"));
    }
}
