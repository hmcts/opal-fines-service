package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.UncategorizedJmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.config.PublishDraftAccountsServiceBusProperties;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.entity.draft.DraftAccountType;
import uk.gov.hmcts.opal.exception.PublishDraftAccountQueueException;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;

@ExtendWith(MockitoExtension.class)
public class PublishDraftAccountPublisherImplTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-10-30T12:00:00Z");
    private static final LocalDateTime FIXED_STATUS_DATE = LocalDateTime.ofInstant(FIXED_INSTANT, ZoneOffset.UTC);
    private static final LocalDateTime INITIAL_STATUS_DATE = FIXED_STATUS_DATE.minusDays(1);
    private static final String QUEUE_NAME = "publish-draft-accounts";
    private static final short BUSINESS_UNIT_ID = 77;

    private static final Long FIRST_ACCOUNT_ID = 1L;
    private static final Long SECOND_ACCOUNT_ID = 2L;
    private static final Long THIRD_ACCOUNT_ID = 3L;
    private static final List<Long> SINGLE_ACCOUNT_IDS = List.of(FIRST_ACCOUNT_ID);
    private static final List<Long> TWO_ACCOUNT_IDS = List.of(FIRST_ACCOUNT_ID, SECOND_ACCOUNT_ID);
    private static final List<Long> THREE_ACCOUNT_IDS = List.of(FIRST_ACCOUNT_ID, SECOND_ACCOUNT_ID, THIRD_ACCOUNT_ID);

    private static final String FIRST_MESSAGE_PAYLOAD = "{\"draft_account_id\":" + FIRST_ACCOUNT_ID + "}";
    private static final String SECOND_MESSAGE_PAYLOAD = "{\"draft_account_id\":" + SECOND_ACCOUNT_ID + "}";
    private static final String THIRD_MESSAGE_PAYLOAD = "{\"draft_account_id\":" + THIRD_ACCOUNT_ID + "}";
    private static final String SEND_ERROR_MESSAGE = "Unable to send publish draft account messages";

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private Session jmsSession;

    @Mock
    private Queue jmsQueue;

    @Mock
    private MessageProducer jmsProducer;

    @Mock
    private TextMessage firstJmsMessage;

    @Mock
    private TextMessage secondJmsMessage;

    @Spy
    private ObjectMapper objectMapper;

    @Mock
    private DraftAccountRepository draftAccountRepository;

    @Mock
    private Clock clock;

    private PublishDraftAccountsServiceBusProperties properties;
    private PublishDraftAccountPublisherImpl publisher;

    @BeforeEach
    void setUp() {
        properties = new PublishDraftAccountsServiceBusProperties();
        properties.setQueueName(QUEUE_NAME);

        publisher = new PublishDraftAccountPublisherImpl(jmsTemplate, objectMapper, properties,
            draftAccountRepository, clock);
    }

    @Test
    void publish_updatesStatusesAndSendsBatch() throws JMSException {
        stubJms();
        stubClock();

        when(jmsSession.createProducer(jmsQueue)).thenReturn(jmsProducer);
        when(jmsSession.createTextMessage(FIRST_MESSAGE_PAYLOAD)).thenReturn(firstJmsMessage);
        when(jmsSession.createTextMessage(SECOND_MESSAGE_PAYLOAD)).thenReturn(secondJmsMessage);

        DraftAccountEntity firstAccount = stubDraftAccount(FIRST_ACCOUNT_ID);
        DraftAccountEntity secondAccount = stubDraftAccount(SECOND_ACCOUNT_ID);

        publisher.publish(TWO_ACCOUNT_IDS);

        assertThat(List.of(firstAccount, secondAccount))
            .isNotEmpty()
            .allSatisfy(account -> {
                assertThat(account.getAccountStatus()).isEqualTo(DraftAccountStatus.PUBLISHING_PENDING);
                assertThat(account.getAccountStatusDate())
                    .isEqualTo(FIXED_STATUS_DATE);
            });

        verify(jmsSession).createQueue(QUEUE_NAME);
        verify(jmsSession).createProducer(jmsQueue);
        verify(jmsSession).createTextMessage(FIRST_MESSAGE_PAYLOAD);
        verify(jmsSession).createTextMessage(SECOND_MESSAGE_PAYLOAD);
        verify(jmsSession).commit();
        verify(jmsSession, never()).rollback();

        verify(jmsProducer).send(firstJmsMessage);
        verify(jmsProducer).send(secondJmsMessage);
        verify(jmsProducer).close();

        verify(jmsTemplate).execute(any(), eq(true));

        verify(draftAccountRepository).flush();
        verify(draftAccountRepository).findById(FIRST_ACCOUNT_ID);
        verify(draftAccountRepository).findById(SECOND_ACCOUNT_ID);
    }

    @Test
    void publish_serialisationFailureStopsProcessing() {
        doThrow(JacksonException.class).when(objectMapper).writeValueAsString(any());

        final DraftAccountEntity firstAccount = stubDraftAccount(FIRST_ACCOUNT_ID);
        stubDraftAccount(SECOND_ACCOUNT_ID);
        IllegalStateException exc = assertThrows(IllegalStateException.class, () -> publisher.publish(TWO_ACCOUNT_IDS));

        assertInstanceOf(JacksonException.class, exc.getCause());
        assertEquals("Unable to serialise publish draft account message", exc.getMessage());
        assertEquals(DraftAccountStatus.SUBMITTED, firstAccount.getAccountStatus());
        verifyNoInteractions(jmsTemplate);
        verify(draftAccountRepository, never()).flush();
    }

    @Test
    void publish_missingAccountPreventsSending() throws JMSException {
        DraftAccountEntity firstAccount = stubDraftAccount(FIRST_ACCOUNT_ID);

        EntityNotFoundException exc = assertThrows(EntityNotFoundException.class,
            () -> publisher.publish(TWO_ACCOUNT_IDS));

        assertEquals("Draft account not found: " + SECOND_ACCOUNT_ID, exc.getMessage());
        assertEquals(DraftAccountStatus.SUBMITTED, firstAccount.getAccountStatus());
        verifyNoInteractions(jmsTemplate);
        verify(jmsSession, never()).createQueue(QUEUE_NAME);
    }

    @Test
    void publish_sendFailureRollsbackBatch() throws JMSException {
        stubJms();
        stubClock();

        when(jmsSession.createProducer(jmsQueue)).thenReturn(jmsProducer);
        when(jmsSession.createTextMessage(FIRST_MESSAGE_PAYLOAD)).thenReturn(firstJmsMessage);
        when(jmsSession.createTextMessage(SECOND_MESSAGE_PAYLOAD)).thenReturn(secondJmsMessage);
        JMSException sendFailure = new JMSException("Second send failed");
        doNothing().when(jmsProducer).send(firstJmsMessage);
        doThrow(sendFailure).when(jmsProducer).send(secondJmsMessage);

        stubDraftAccount(FIRST_ACCOUNT_ID);
        stubDraftAccount(SECOND_ACCOUNT_ID);
        stubDraftAccount(THIRD_ACCOUNT_ID);

        PublishDraftAccountQueueException exc = assertThrows(PublishDraftAccountQueueException.class,
            () -> publisher.publish(THREE_ACCOUNT_IDS));

        assertEquals(SEND_ERROR_MESSAGE, exc.getMessage());
        assertSame(sendFailure, exc.getCause());
        verify(jmsProducer).send(firstJmsMessage);
        verify(jmsProducer).send(secondJmsMessage);
        verify(jmsProducer).close();
        verify(jmsSession, never()).createTextMessage(THIRD_MESSAGE_PAYLOAD);
        verify(jmsSession).rollback();
        verify(jmsSession, never()).commit();
    }

    @Test
    void publish_jmsOperationFailureRollsback() throws JMSException {
        stubJms();
        stubClock();

        JMSException producerFailure = new JMSException("Unable to create producer");
        when(jmsSession.createProducer(jmsQueue)).thenThrow(producerFailure);

        stubDraftAccount(FIRST_ACCOUNT_ID);

        PublishDraftAccountQueueException exc = assertThrows(PublishDraftAccountQueueException.class,
            () -> publisher.publish(SINGLE_ACCOUNT_IDS));

        assertEquals(SEND_ERROR_MESSAGE, exc.getMessage());
        assertSame(producerFailure, exc.getCause());
        verify(jmsSession).rollback();
        verify(jmsSession, never()).commit();
        verify(jmsProducer, never()).send(any(TextMessage.class));
    }

    @Test
    void publish_commitFailureAttemptsRollback() throws JMSException {
        stubJms();
        stubClock();

        when(jmsSession.createProducer(jmsQueue)).thenReturn(jmsProducer);
        when(jmsSession.createTextMessage(FIRST_MESSAGE_PAYLOAD)).thenReturn(firstJmsMessage);
        JMSException commitFailure = new JMSException("Commit failed");
        doThrow(commitFailure).when(jmsSession).commit();

        stubDraftAccount(FIRST_ACCOUNT_ID);

        PublishDraftAccountQueueException exc = assertThrows(PublishDraftAccountQueueException.class,
            () -> publisher.publish(SINGLE_ACCOUNT_IDS));

        assertEquals(SEND_ERROR_MESSAGE, exc.getMessage());
        assertSame(commitFailure, exc.getCause());
        verify(jmsProducer).send(firstJmsMessage);
        verify(jmsProducer).close();
        verify(jmsSession).commit();
        verify(jmsSession).rollback();
    }

    @Test
    void publish_rollbackFailurePreservesOriginalException() throws JMSException {
        stubJms();
        stubClock();

        when(jmsSession.createProducer(jmsQueue)).thenReturn(jmsProducer);
        when(jmsSession.createTextMessage(FIRST_MESSAGE_PAYLOAD)).thenReturn(firstJmsMessage);
        JMSException sendFailure = new JMSException("Send failed");
        JMSException rollbackFailure = new JMSException("Rollback failed");
        doThrow(sendFailure).when(jmsProducer).send(firstJmsMessage);
        doThrow(rollbackFailure).when(jmsSession).rollback();

        stubDraftAccount(FIRST_ACCOUNT_ID);

        PublishDraftAccountQueueException exc = assertThrows(PublishDraftAccountQueueException.class,
            () -> publisher.publish(SINGLE_ACCOUNT_IDS));

        assertEquals(SEND_ERROR_MESSAGE, exc.getMessage());
        assertSame(sendFailure, exc.getCause());
        assertThat(sendFailure.getSuppressed()).containsExactly(rollbackFailure);
        verify(jmsProducer).close();
        verify(jmsSession).rollback();
        verify(jmsSession, never()).commit();
    }

    @Test
    void publish_connectionFailurePropogates() throws JMSException {
        stubClock();

        UncategorizedJmsException connectionFailure = new UncategorizedJmsException("Connection failed");
        doThrow(connectionFailure).when(jmsTemplate).execute(any(), eq(true));

        stubDraftAccount(FIRST_ACCOUNT_ID);

        UncategorizedJmsException exc = assertThrows(UncategorizedJmsException.class,
            () -> publisher.publish(SINGLE_ACCOUNT_IDS));

        assertSame(connectionFailure, exc);
        verify(draftAccountRepository).flush();
        verify(jmsSession, never()).createQueue(QUEUE_NAME);
        verify(jmsSession, never()).commit();
        verify(jmsSession, never()).rollback();
    }

    private void stubClock() {
        when(clock.instant()).thenReturn(FIXED_INSTANT);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    private void stubJms() throws JMSException {
        when(jmsTemplate.<Void>execute(any(), eq(true))).thenAnswer(invocation -> {
            SessionCallback<Void> callback = invocation.getArgument(0);
            return callback.doInJms(jmsSession);
        });

        when(jmsSession.createQueue(QUEUE_NAME)).thenReturn(jmsQueue);
    }

    private DraftAccountEntity stubDraftAccount(Long draftAccountId) {
        DraftAccountEntity account = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId(BUSINESS_UNIT_ID).build())
            .submittedBy("submitter")
            .accountType(DraftAccountType.FINE)
            .accountSnapshot("{\"snapshot\":\"draft account " + draftAccountId + "\"}")
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .accountStatusDate(INITIAL_STATUS_DATE)
            .build();
        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(account));
        return account;
    }

}
