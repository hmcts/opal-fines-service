package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
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
import jakarta.jms.TextMessage;
import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
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
import uk.gov.hmcts.opal.dto.DraftAccountSummaryDto;
import uk.gov.hmcts.opal.dto.DraftAccountsResponseDto;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.entity.draft.DraftAccountType;
import uk.gov.hmcts.opal.exception.PublishDraftAccountQueueException;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;

@ExtendWith(MockitoExtension.class)
public class PublishDraftAccountPublisherImplTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-10-30T12:00:00Z");
    private static final String QUEUE_NAME = "publish-draft-accounts";

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
        when(jmsSession.createTextMessage(contains("\"draft_account_id\":1"))).thenReturn(firstJmsMessage);
        when(jmsSession.createTextMessage(contains("\"draft_account_id\":2"))).thenReturn(secondJmsMessage);

        DraftAccountsResponseDto draftAccounts = createDraftAccountsResponse(1L, 2L);
        DraftAccountEntity firstAccount = stubDraftAccount(1L);
        DraftAccountEntity secondAccount = stubDraftAccount(2L);

        publisher.publish(draftAccounts);

        assertThat(List.of(firstAccount, secondAccount))
            .isNotEmpty()
            .allSatisfy(account -> {
                assertThat(account.getAccountStatus()).isEqualTo(DraftAccountStatus.PUBLISHING_PENDING);
                assertThat(account.getAccountStatusDate())
                    .isEqualTo(LocalDateTime.ofInstant(FIXED_INSTANT, ZoneOffset.UTC));
            });

        verify(jmsSession).createQueue(QUEUE_NAME);
        verify(jmsSession).createProducer(jmsQueue);
        verify(jmsSession).commit();
        verify(jmsSession, never()).rollback();

        verify(jmsProducer).send(firstJmsMessage);
        verify(jmsProducer).send(secondJmsMessage);
        verify(jmsProducer).close();

        verify(jmsTemplate).execute(any(), eq(true));

        verify(draftAccountRepository).flush();
    }

    @Test
    void publish_serialisationFailureStopsProcessing() {
        doThrow(JacksonException.class).when(objectMapper).writeValueAsString(any());

        DraftAccountsResponseDto draftAccounts = createDraftAccountsResponse(1L, 2L);
        IllegalStateException exc = assertThrows(IllegalStateException.class, () -> publisher.publish(draftAccounts));

        assertInstanceOf(JacksonException.class, exc.getCause());
        assertEquals("Unable to serialise publish draft account message", exc.getMessage());
    }

    @Test
    void publish_missingAccountPreventsSending() throws JMSException {
        stubClock();

        DraftAccountsResponseDto draftAccounts = createDraftAccountsResponse(1L, 2L);
        stubDraftAccount(1L);

        EntityNotFoundException exc = assertThrows(EntityNotFoundException.class,
            () -> publisher.publish(draftAccounts));

        assertEquals("Draft account not found: 2", exc.getMessage());

        verify(jmsSession, never()).createQueue(QUEUE_NAME);
    }

    @Test
    void publish_sendFailureRollsbackBatch() throws JMSException {
        stubJms();
        stubClock();

        when(jmsSession.createProducer(jmsQueue)).thenReturn(jmsProducer);
        when(jmsSession.createTextMessage(contains("\"draft_account_id\":1"))).thenReturn(firstJmsMessage);
        when(jmsSession.createTextMessage(contains("\"draft_account_id\":2"))).thenReturn(secondJmsMessage);
        JMSException sendFailure = new JMSException("Second send failed");
        doNothing().when(jmsProducer).send(firstJmsMessage);
        doThrow(sendFailure).when(jmsProducer).send(secondJmsMessage);

        stubDraftAccount(1L);
        stubDraftAccount(2L);
        stubDraftAccount(3L);
        DraftAccountsResponseDto draftAccounts = createDraftAccountsResponse(1L, 2L, 3L);

        PublishDraftAccountQueueException exc = assertThrows(PublishDraftAccountQueueException.class,
            () -> publisher.publish(draftAccounts));

        assertEquals("Unable to send publish draft account messages", exc.getMessage());
        assertSame(sendFailure, exc.getCause());
        verify(jmsProducer).send(firstJmsMessage);
        verify(jmsProducer).send(secondJmsMessage);
        verify(jmsProducer).close();
        verify(jmsSession, never()).createTextMessage(contains("\"draft_account_id\":3"));
        verify(jmsSession).rollback();
        verify(jmsSession, never()).commit();
    }

    @Test
    void publish_jmsOperationFailureRollsback() throws JMSException {
        stubJms();
        stubClock();

        JMSException producerFailure = new JMSException("Unable to create producer");
        when(jmsSession.createProducer(jmsQueue)).thenThrow(producerFailure);

        DraftAccountsResponseDto draftAccounts = createDraftAccountsResponse(1L);
        stubDraftAccount(1L);

        PublishDraftAccountQueueException exc = assertThrows(PublishDraftAccountQueueException.class,
            () -> publisher.publish(draftAccounts));

        assertEquals("Unable to send publish draft account messages", exc.getMessage());
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
        when(jmsSession.createTextMessage(contains("\"draft_account_id\":1"))).thenReturn(firstJmsMessage);
        JMSException commitFailure = new JMSException("Commit failed");
        doThrow(commitFailure).when(jmsSession).commit();

        DraftAccountsResponseDto draftAccounts = createDraftAccountsResponse(1L);
        stubDraftAccount(1L);

        PublishDraftAccountQueueException exc = assertThrows(PublishDraftAccountQueueException.class,
            () -> publisher.publish(draftAccounts));

        assertEquals("Unable to send publish draft account messages", exc.getMessage());
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
        when(jmsSession.createTextMessage(contains("\"draft_account_id\":1"))).thenReturn(firstJmsMessage);
        JMSException sendFailure = new JMSException("Send failed");
        JMSException rollbackFailure = new JMSException("Rollback failed");
        doThrow(sendFailure).when(jmsProducer).send(firstJmsMessage);
        doThrow(rollbackFailure).when(jmsSession).rollback();

        DraftAccountsResponseDto draftAccounts = createDraftAccountsResponse(1L);
        stubDraftAccount(1L);

        PublishDraftAccountQueueException exc = assertThrows(PublishDraftAccountQueueException.class,
            () -> publisher.publish(draftAccounts));

        assertEquals("Unable to send publish draft account messages", exc.getMessage());
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

        DraftAccountsResponseDto draftAccounts = createDraftAccountsResponse(1L);
        stubDraftAccount(1L);

        UncategorizedJmsException exc = assertThrows(UncategorizedJmsException.class,
            () -> publisher.publish(draftAccounts));

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
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .accountStatusDate(LocalDateTime.ofInstant(FIXED_INSTANT, ZoneOffset.UTC).minusDays(1))
            .build();
        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(account));
        return account;
    }

    private DraftAccountsResponseDto createDraftAccountsResponse(Long... draftAccountIds) {
        return DraftAccountsResponseDto.builder()
            .summaries(Arrays.stream(draftAccountIds)
                .map(id -> DraftAccountSummaryDto.builder()
                    .draftAccountId(id)
                    .businessUnitId((short) 77)
                    .submittedBy("submitter")
                    .accountType(DraftAccountType.FINE)
                    .accountStatus(DraftAccountStatus.SUBMITTED)
                    .build())
                .toList())
            .build();
    }

}
