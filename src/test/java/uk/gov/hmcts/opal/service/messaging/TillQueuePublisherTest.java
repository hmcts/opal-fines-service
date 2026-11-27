package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.config.TillQueueProperties;

@ExtendWith(MockitoExtension.class)
class TillQueuePublisherTest {

    private static final List<TillQueueMessage> BATCH_MESSAGES = List.of(
        new TillQueueMessage(12L, (short) 77, "BU-USER-77", "opal_user01", 42L),
        new TillQueueMessage(13L, (short) 78, "BU-USER-78", "opal_user02", 42L));

    @Mock
    private ConnectionFactory connectionFactory;
    @Mock
    private Connection connection;
    @Mock
    private Session session;
    @Mock
    private Queue queue;
    @Mock
    private MessageProducer producer;
    @Mock
    private TextMessage firstMessage;
    @Mock
    private TextMessage secondMessage;

    private JmsTemplate template;
    private TillQueueProperties properties;
    private TillQueuePublisher publisher;

    @BeforeEach
    void setUp() {
        properties = new TillQueueProperties();
        properties.setQueueName("allocate-tills");
        template = new JmsTemplate(connectionFactory);
        template.setSessionTransacted(true);
        publisher = new TillQueuePublisher(template, new ObjectMapper(), properties);
    }

    @Test
    void commitsCompleteBatch() throws JMSException {
        prepareBatch();

        publisher.publish(BATCH_MESSAGES);

        verify(producer).send(firstMessage);
        verify(producer).send(secondMessage);
        verify(producer).close();
        verify(session).commit();
        verify(session, never()).rollback();
    }

    @Test
    void rollsBackWhenLaterSendFails() throws JMSException {
        prepareBatch();
        JMSException failure = new JMSException("Second send failed");
        doNothing().when(producer).send(firstMessage);
        doThrow(failure).when(producer).send(secondMessage);

        assertThatThrownBy(() -> publisher.publish(BATCH_MESSAGES))
            .isInstanceOf(JmsException.class).hasCause(failure);

        verify(producer).send(firstMessage);
        verify(producer).close();
        verify(session).rollback();
        verify(session, never()).commit();
    }

    @Test
    void rollsBackWhenCommitFails() throws JMSException {
        prepareBatch();
        JMSException failure = new JMSException("Commit failed");
        doThrow(failure).when(session).commit();

        assertThatThrownBy(() -> publisher.publish(BATCH_MESSAGES))
            .isInstanceOf(JmsException.class).hasCause(failure);

        verify(session).rollback();
    }

    @Test
    void preservesFailureWhenRollbackFails() throws JMSException {
        prepareBatch();
        IllegalStateException failure = new IllegalStateException("Send failed");
        JMSException rollbackFailure = new JMSException("Rollback failed");
        doNothing().when(producer).send(firstMessage);
        doThrow(failure).when(producer).send(secondMessage);
        doThrow(rollbackFailure).when(session).rollback();

        assertThatThrownBy(() -> publisher.publish(BATCH_MESSAGES))
            .isSameAs(failure).hasSuppressedException(rollbackFailure);

        verify(session, never()).commit();
    }

    @Test
    void serializationFailureDoesNotOpenConnection() {
        ObjectMapper mapper = mock(ObjectMapper.class);
        JacksonException failure = mock(JacksonException.class);
        when(mapper.writeValueAsString(BATCH_MESSAGES.getFirst()))
            .thenReturn("{\"till_id\":12,\"business_unit_id\":77,\"posted_by\":\"BU-USER-77\","
                + "\"posted_by_name\":\"opal_user01\",\"requested_by\":42}");
        when(mapper.writeValueAsString(BATCH_MESSAGES.get(1))).thenThrow(failure);
        publisher = new TillQueuePublisher(template, mapper, properties);

        assertThatThrownBy(() -> publisher.publish(BATCH_MESSAGES))
            .isInstanceOf(IllegalStateException.class).hasCause(failure);

        verifyNoInteractions(connectionFactory);
    }

    @Test
    void propagatesConnectionFailure() throws JMSException {
        JMSException failure = new JMSException("Unavailable");
        when(connectionFactory.createConnection()).thenThrow(failure);

        assertThatThrownBy(() -> publisher.publish(List.of(BATCH_MESSAGES.getFirst())))
            .isInstanceOf(JmsException.class).hasCause(failure);
    }

    private void prepareBatch() throws JMSException {
        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(true, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createQueue("allocate-tills")).thenReturn(queue);
        when(session.createProducer(queue)).thenReturn(producer);
        when(session.createTextMessage("{\"till_id\":12,\"business_unit_id\":77,"
            + "\"posted_by\":\"BU-USER-77\",\"posted_by_name\":\"opal_user01\",\"requested_by\":42}"))
            .thenReturn(firstMessage);
        when(session.createTextMessage("{\"till_id\":13,\"business_unit_id\":78,"
            + "\"posted_by\":\"BU-USER-78\",\"posted_by_name\":\"opal_user02\",\"requested_by\":42}"))
            .thenReturn(secondMessage);
    }
}
