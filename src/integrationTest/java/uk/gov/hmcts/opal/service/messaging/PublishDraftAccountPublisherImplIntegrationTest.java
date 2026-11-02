package uk.gov.hmcts.opal.service.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.config.PublishDraftAccountsServiceBusProperties;
import uk.gov.hmcts.opal.dto.DraftAccountSummaryDto;
import uk.gov.hmcts.opal.dto.DraftAccountsResponseDto;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.exception.PublishDraftAccountQueueException;
import uk.gov.hmcts.opal.mapper.DraftAccountMapper;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Sql(scripts = "classpath:db/insertData/insert_into_draft_accounts.sql",
    executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_draft_accounts.sql",
    executionPhase = AFTER_TEST_METHOD)
@DisplayName("Publish Draft Account Publisher Impl Integration Test")
public class PublishDraftAccountPublisherImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DraftAccountRepository repository;

    @Autowired
    private PublishDraftAccountPublisherImpl publisher;

    @Autowired
    private DraftAccountMapper mapper;

    @Autowired
    private PublishDraftAccountsServiceBusProperties properties;

    @MockitoSpyBean(name = "publishDraftAccountPublisherJmsTemplate")
    private JmsTemplate jmsTemplate;

    private Session jmsSession;
    private Queue jmsQueue;
    private MessageProducer jmsProducer;

    @Test
    @JiraStory("PO-10663")
    @JiraEpic("PO-7462")
    @DisplayName("AC1 - Publishes every summary and commits pending statuses")
    void publish_sendsAllSummariesAndCommitsPendingStatuses() throws JMSException {
        stubJms();

        DraftAccountsResponseDto draftAccounts = createDraftAccountsResponse(1L, 2L, 3L);
        List<DraftAccountEntity> originalAccounts = repository.findAllById(List.of(1L, 2L, 3L));
        assertEquals(3, originalAccounts.size());

        publisher.publish(draftAccounts);

        assertSendAttempts(draftAccounts.getSummaries());
        assertPendingStatuses(originalAccounts);

        verify(jmsTemplate).execute(any(), eq(true));
        verify(jmsSession).createQueue(properties.getQueueName());
        verify(jmsSession).createProducer(jmsQueue);
        verify(jmsSession).commit();
        verify(jmsSession, never()).rollback();
        verify(jmsProducer).close();
    }

    @Test
    @JiraStory("PO-10663")
    @JiraEpic("PO-7462")
    @DisplayName("AC2 - A send failure rolls back the batch and all status updates")
    void publish_sendFailureRollsBackBatchAndStatusUpdates() throws JMSException {
        stubJms();

        JMSException sendFailure = new JMSException("Second send failed");
        doNothing().doThrow(sendFailure).when(jmsProducer).send(any(Message.class));

        DraftAccountsResponseDto draftAccounts = createDraftAccountsResponse(1L, 2L, 3L);
        List<DraftAccountEntity> originalAccounts = repository.findAllById(List.of(1L, 2L, 3L));
        assertEquals(3, originalAccounts.size());

        PublishDraftAccountQueueException exception = assertThrows(PublishDraftAccountQueueException.class,
            () -> publisher.publish(draftAccounts));

        assertSame(sendFailure, exception.getCause());
        assertSendAttempts(draftAccounts.getSummaries().subList(0, 2));

        verify(jmsTemplate).execute(any(), eq(true));
        verify(jmsSession, times(2)).createTextMessage(anyString());
        verify(jmsSession).rollback();
        verify(jmsSession, never()).commit();
        verify(jmsProducer).close();

        assertAccountStateUnchanged(originalAccounts);
    }

    private void assertSendAttempts(List<DraftAccountSummaryDto> expectedSummaries) throws JMSException {
        ArgumentCaptor<TextMessage> messages = ArgumentCaptor.forClass(TextMessage.class);

        verify(jmsProducer, times(expectedSummaries.size())).send(messages.capture());

        for (int i = 0; i < expectedSummaries.size(); i++) {
            assertEquals(objectMapper.writeValueAsString(expectedSummaries.get(i)),
                messages.getAllValues().get(i).getText());
        }
    }

    private void assertPendingStatuses(List<DraftAccountEntity> originals) {
        LocalDateTime statusDate = repository.findById(originals.getFirst().getDraftAccountId())
            .orElseThrow().getAccountStatusDate();

        for (DraftAccountEntity original : originals) {
            DraftAccountEntity updated = repository.findById(original.getDraftAccountId()).orElseThrow();

            assertEquals(DraftAccountStatus.PUBLISHING_PENDING, updated.getAccountStatus());
            assertTrue(updated.getAccountStatusDate().isAfter(original.getAccountStatusDate()));
            assertEquals(statusDate, updated.getAccountStatusDate());
            assertEquals(original.getVersionNumber() + 1, updated.getVersionNumber());
        }
    }

    private void assertAccountStateUnchanged(List<DraftAccountEntity> originals) {
        for (DraftAccountEntity original : originals) {
            DraftAccountEntity unchanged = repository.findById(original.getDraftAccountId()).orElseThrow();

            assertEquals(original.getAccountStatus(), unchanged.getAccountStatus());
            assertEquals(original.getAccountStatusDate(), unchanged.getAccountStatusDate());
            assertEquals(original.getVersionNumber(), unchanged.getVersionNumber());
            assertEquals(original.getStatusMessage(), unchanged.getStatusMessage());
        }
    }

    private void stubJms() throws JMSException {
        jmsSession = mock(Session.class);
        jmsQueue = mock(Queue.class);
        jmsProducer = mock(MessageProducer.class);

        doAnswer(invocation -> {
            SessionCallback<Void> callback = invocation.getArgument(0);
            return callback.doInJms(jmsSession);
        }).when(jmsTemplate).<Void>execute(any(), eq(true));

        when(jmsSession.createQueue(properties.getQueueName())).thenReturn(jmsQueue);
        when(jmsSession.createProducer(jmsQueue)).thenReturn(jmsProducer);
        when(jmsSession.createTextMessage(anyString())).thenAnswer(invocation -> {
            TextMessage message = mock(TextMessage.class);
            when(message.getText()).thenReturn(invocation.getArgument(0));
            return message;
        });
    }

    private DraftAccountsResponseDto createDraftAccountsResponse(Long... draftAccountIds) {
        return DraftAccountsResponseDto.builder()
            .summaries(Arrays.stream(draftAccountIds)
                .map(id -> repository.findById(id).orElseThrow())
                .map(mapper::toDto)
                .toList())
            .build();
    }

}
