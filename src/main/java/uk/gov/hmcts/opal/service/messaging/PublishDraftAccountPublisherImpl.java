package uk.gov.hmcts.opal.service.messaging;

import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.config.PublishDraftAccountsServiceBusProperties;
import uk.gov.hmcts.opal.dto.DraftAccountSummaryDto;
import uk.gov.hmcts.opal.dto.DraftAccountsResponseDto;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.exception.PublishDraftAccountQueueException;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;

@Component
@Slf4j(topic = "opal.PublishDraftAccountPublisher")
@RequiredArgsConstructor
public class PublishDraftAccountPublisherImpl implements PublishDraftAccountPublisherInterface {

    @Qualifier("publishDraftAccountPublisherJmsTemplate")
    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    private final PublishDraftAccountsServiceBusProperties properties;
    private final DraftAccountRepository draftAccountRepository;
    private final Clock clock;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(DraftAccountsResponseDto draftAccounts) {
        List<String> payloads = draftAccounts.getSummaries()
            .stream()
            .map(this::toPayload)
            .toList();

        updateStatuses(draftAccounts);
        draftAccountRepository.flush();

        jmsTemplate.execute(session -> sendBatch(session, payloads), true);
    }

    private String toPayload(DraftAccountSummaryDto draftAccount) {
        try {
            return objectMapper.writeValueAsString(draftAccount);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Unable to serialise publish draft account message", exception);
        }
    }

    private Void sendBatch(Session session, List<String> payloads) throws PublishDraftAccountQueueException {
        try {
            sendMessages(session, payloads);
            session.commit();
        } catch (JMSException exception) {
            throw new PublishDraftAccountQueueException(
                "Unable to send publish draft account messages",
                rollback(session, exception));
        }

        return null;
    }

    private void sendMessages(Session session, List<String> payloads) throws JMSException {
        try (MessageProducer producer = session.createProducer(session.createQueue(properties.getQueueName()))) {
            for (String payload : payloads) {
                producer.send(session.createTextMessage(payload));
            }
        }
    }

    private JMSException rollback(Session session, JMSException cause) {
        try {
            session.rollback();
        } catch (JMSException rollbackException) {
            cause.addSuppressed(rollbackException);
        }

        return cause;
    }

    private void updateStatuses(DraftAccountsResponseDto draftAccounts) {
        LocalDateTime statusDate = LocalDateTime.now(clock);

        for (var draftAccount : draftAccounts.getSummaries()) {
            DraftAccountEntity account = draftAccountRepository.findById(draftAccount.getDraftAccountId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Draft account not found: " + draftAccount.getDraftAccountId()));

            account.setAccountStatus(DraftAccountStatus.PUBLISHING_PENDING);
            account.setAccountStatusDate(statusDate);
        }
    }
}
