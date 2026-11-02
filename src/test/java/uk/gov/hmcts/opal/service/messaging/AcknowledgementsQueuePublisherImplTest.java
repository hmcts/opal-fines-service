package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.config.AcknowledgementsServiceBusProperties;

@ExtendWith(MockitoExtension.class)
public class AcknowledgementsQueuePublisherImplTest {
    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private Session session;

    @Mock
    private Queue destination;

    @Mock
    private MessageProducer producer;

    @Mock
    private jakarta.jms.TextMessage message;

    private AcknowledgementsServiceBusProperties properties;
    private AcknowledgementsQueuePublisherImpl publisher;

    private static final String QUEUE_NAME = "vpfpo-acknowledgements-queue";
    private static final String SUCCESS_MESSAGE = """
        {"vpfpo_registration_id":1,"defendant_id":2,"status":"PUBLISHED"}""";
    private static final String ERROR_MESSAGE = """
        {"vpfpo_registration_id":1,"status":"PUBLISHING_FAILED"}""";

    @BeforeEach
    void setUp() throws JMSException {
        properties = new AcknowledgementsServiceBusProperties();
        properties.setQueueName(QUEUE_NAME);

        when(jmsTemplate.<Void>execute(
            ArgumentMatchers.any(), eq(true))).thenAnswer(invocation -> {
            SessionCallback<Void> callback = invocation.getArgument(0);
            return callback.doInJms(session);
        });
        when(session.createQueue(QUEUE_NAME)).thenReturn(destination);

        publisher = new AcknowledgementsQueuePublisherImpl(jmsTemplate, new ObjectMapper(), properties);
    }

    @Test
    @DisplayName(".publish() adds a successful message to the queue")
    void addsSuccessMessageToAcknowledgementsQueue() throws JMSException {
        when(session.createProducer(destination)).thenReturn(producer);
        when(session.createTextMessage(SUCCESS_MESSAGE)).thenReturn(message);

        publisher.publish(1L, 2L);

        verify(session).createQueue(QUEUE_NAME);
        verify(session).createProducer(destination);
        verify(session).createTextMessage(SUCCESS_MESSAGE);
        verify(producer).send(message);
        verify(producer).close();
        verify(session).commit();
        verify(session, never()).rollback();
        verify(jmsTemplate).execute(ArgumentMatchers.<SessionCallback<Void>>any(), eq(true));
    }

    @Test
    @DisplayName(".publishError() adds an error message to the queue")
    void addsErrorMessageToAcknowledgementsQueue() throws JMSException {
        when(session.createProducer(destination)).thenReturn(producer);
        when(session.createTextMessage(ERROR_MESSAGE)).thenReturn(message);

        publisher.publishError(1L);

        verify(session).createQueue(QUEUE_NAME);
        verify(session).createProducer(destination);
        verify(session).createTextMessage(ERROR_MESSAGE);
        verify(producer).send(message);
        verify(producer).close();
        verify(session).commit();
        verify(session, never()).rollback();
        verify(jmsTemplate).execute(ArgumentMatchers.<SessionCallback<Void>>any(), eq(true));
    }

    @Test
    @DisplayName(".publish() rolls back and adds nothing to the queue on an exception")
    void rollsbackWhenSuccessfullMessageThrowsError() throws JMSException {
        when(session.createProducer(destination)).thenReturn(producer);
        when(session.createTextMessage(SUCCESS_MESSAGE)).thenReturn(message);
        doThrow(new JMSException("publisher failed")).when(producer).send(message);

        JMSException e = assertThrows(
            JMSException.class,
            () -> publisher.publish(1L, 2L)
        );

        assertThat(e.getMessage()).isEqualTo("publisher failed");
        verify(session).createQueue(QUEUE_NAME);
        verify(session).createProducer(destination);
        verify(session).createTextMessage(SUCCESS_MESSAGE);
        verify(producer).send(message);
        verify(producer).close();
        verify(session, never()).commit();
        verify(session, times(1)).rollback();
        verify(jmsTemplate).execute(ArgumentMatchers.<SessionCallback<Void>>any(), eq(true));
    }

    @Test
    @DisplayName(".publishError() rolls back and adds nothing to the queue on an exception")
    void rollsbackWhenErrorMessageThrowsError() throws JMSException {
        when(session.createProducer(destination)).thenReturn(producer);
        when(session.createTextMessage(ERROR_MESSAGE)).thenReturn(message);
        doThrow(new JMSException("publisher failed")).when(producer).send(message);

        JMSException e = assertThrows(
            JMSException.class,
            () -> publisher.publishError(1L)
        );

        assertThat(e.getMessage()).isEqualTo("publisher failed");
        verify(session).createQueue(QUEUE_NAME);
        verify(session).createProducer(destination);
        verify(session).createTextMessage(ERROR_MESSAGE);
        verify(producer).send(message);
        verify(producer).close();
        verify(session, never()).commit();
        verify(session, times(1)).rollback();
        verify(jmsTemplate).execute(ArgumentMatchers.<SessionCallback<Void>>any(), eq(true));
    }
}
