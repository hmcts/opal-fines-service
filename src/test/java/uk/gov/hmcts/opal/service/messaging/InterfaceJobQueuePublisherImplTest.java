package uk.gov.hmcts.opal.service.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.config.InterfaceJobServiceBusProperties;
import uk.gov.hmcts.opal.exception.InterfaceJobQueueException;

@ExtendWith(MockitoExtension.class)
class InterfaceJobQueuePublisherImplTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private Session session;

    @Mock
    private Queue destination;

    @Mock
    private MessageProducer producer;

    @Mock
    private jakarta.jms.TextMessage firstMessage;

    @Mock
    private jakarta.jms.TextMessage secondMessage;

    private InterfaceJobServiceBusProperties properties;
    private InterfaceJobQueuePublisherImpl publisher;

    @BeforeEach
    void setUp() throws JMSException {
        properties = new InterfaceJobServiceBusProperties();
        properties.setQueueName("process-interface-files");

        when(jmsTemplate.<Void>execute(
            ArgumentMatchers.any(), eq(true))).thenAnswer(invocation -> {
                SessionCallback<Void> callback = invocation.getArgument(0);
                return callback.doInJms(session);
            });
        when(session.createQueue("process-interface-files")).thenReturn(destination);

        publisher = new InterfaceJobQueuePublisherImpl(jmsTemplate, new ObjectMapper(), properties);
    }

    @Test
    void publish_executesTheCompleteBatchInOneTransactedJmsOperation() throws JMSException {
        when(session.createProducer(destination)).thenReturn(producer);
        when(session.createTextMessage("{\"interface_job_id\":12}")).thenReturn(firstMessage);
        when(session.createTextMessage("{\"interface_job_id\":13}")).thenReturn(secondMessage);

        publisher.publish(List.of(12L, 13L));

        verify(session).createQueue("process-interface-files");
        verify(session).createProducer(destination);
        verify(session).createTextMessage("{\"interface_job_id\":12}");
        verify(session).createTextMessage("{\"interface_job_id\":13}");
        verify(producer).send(firstMessage);
        verify(producer).send(secondMessage);
        verify(producer).close();
        verify(session).commit();
        verify(session, never()).rollback();
        verify(jmsTemplate).execute(ArgumentMatchers.<SessionCallback<Void>>any(), eq(true));
    }

    @Test
    void publish_propagatesLaterSendFailureForTransactedOperation() throws JMSException {
        when(session.createProducer(destination)).thenReturn(producer);
        when(session.createTextMessage("{\"interface_job_id\":12}")).thenReturn(firstMessage);
        when(session.createTextMessage("{\"interface_job_id\":13}")).thenReturn(secondMessage);
        doNothing().when(producer).send(firstMessage);
        doThrow(new JMSException("broker send failed")).when(producer).send(secondMessage);

        InterfaceJobQueueException exception = assertThrows(
            InterfaceJobQueueException.class, () -> publisher.publish(List.of(12L, 13L, 14L)));

        assertEquals("Unable to send interface job messages", exception.getMessage());

        verify(session).createQueue("process-interface-files");
        verify(session).createProducer(destination);
        verify(producer).send(firstMessage);
        verify(producer).send(secondMessage);
        verify(session, never()).createTextMessage("{\"interface_job_id\":14}");
        verify(producer).close();
        verify(session).rollback();
        verify(session, never()).commit();
        verify(jmsTemplate).execute(ArgumentMatchers.<SessionCallback<Void>>any(), eq(true));
    }
}
