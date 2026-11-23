package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static uk.gov.hmcts.opal.common.dto.ToJsonString.toJsonString;

import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.apache.qpid.jms.provider.amqp.message.AmqpJmsTextMessageFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;
import uk.gov.hmcts.opal.config.QueueConsumerJmsConfig;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;
import uk.gov.hmcts.opal.service.messaging.AcknowledgementsQueueMessage.Status;
import uk.gov.hmcts.opal.support.AcknowledgementsTestPublisher;
import uk.gov.hmcts.opal.support.TestAuthConfig;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {
    "opal.publish-draft.service-bus.consumer-enabled=false",
    "opal.publish-draft.service-bus.queue-name=publish-draft-account-queue",
    "opal.report.service-bus.consumer-enabled=true",
    "opal.publish-draft.service-bus.consumer-enabled=true",
    "opal.automated-task=",
})
@Sql(scripts = "classpath:db/insertData/insert_into_draft_accounts_vpfpo_publishing.sql",
    executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_draft_accounts_vpfpo.sql",
    executionPhase = AFTER_TEST_METHOD)
@Import({QueueConsumerJmsConfig.class, TestAuthConfig.class})
class PublishDraftAccountQueueConsumerIntegrationTest extends AbstractIntegrationWithSecurityTest {

    @Autowired
    private PublishDraftAccountListener listener;

    @Autowired
    private DraftAccountRepository repository;

    @Autowired
    private AcknowledgementsTestPublisher acknowledgementsTestPublisher;

    private static final long VALID_DRAFT_ACCOUNT_ID = 21L;
    private static final long FAILING_DRAFT_ACCOUNT_ID = 31L;

    @BeforeEach
    void setup() {
        acknowledgementsTestPublisher.clearMessages();
    }

    @Test
    @DisplayName("AC1 - Valid message is processed successfully")
    @JiraStory("PO-10665")
    @JiraEpic("PO-7462")
    void validMessageIsProcessedSuccessfully() throws Exception {
        PublishDraftAccountQueueMessage message = publishDraftAccountQueueMessage(VALID_DRAFT_ACCOUNT_ID);
        queueMessage(message);

        DraftAccountEntity publishedEntity = assertAccountPublished(VALID_DRAFT_ACCOUNT_ID);
        assertSuccessfullAcknowledgementsMessage(800L, publishedEntity.getAccountId());
        assertNoMoreAcknowledgementsMessages();
    }

    @Test
    @DisplayName("AC2 - Failed message is processed successfully")
    @JiraStory("PO-10665")
    @JiraEpic("PO-7462")
    void failingMessageIsProcessedSuccessfully() throws Exception {
        PublishDraftAccountQueueMessage message = publishDraftAccountQueueMessage(FAILING_DRAFT_ACCOUNT_ID);
        queueMessage(message);

        assertAccountNotPublished(FAILING_DRAFT_ACCOUNT_ID);
        assertFailedAcknowledgementsMessage(800L);
        assertNoMoreAcknowledgementsMessages();
    }

    @Test
    @DisplayName("Fails when draft account cannot be found")
    @JiraStory("PO-10665")
    @JiraEpic("PO-7462")
    void DraftAccountNotFoundFails() throws Exception {
        PublishDraftAccountQueueMessage message = publishDraftAccountQueueMessage(400L);
        assertThatThrownBy(() -> queueMessage(message))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("could not find draft account with id 400");

        assertNoMoreAcknowledgementsMessages();
    }



    private DraftAccountEntity assertAccountPublished(long id) {
        Optional<DraftAccountEntity> entity = repository.findById(id);

        assertThat(entity).isPresent();
        assertThat(entity.get().getAccountStatus()).isEqualTo(DraftAccountStatus.PUBLISHED);
        assertThat(entity.get().getAccountId()).isNotNull();
        return entity.get();
    }

    private void assertAccountNotPublished(long id) {
        Optional<DraftAccountEntity> entity = repository.findById(id);

        assertThat(entity).isPresent();
        assertThat(entity.get().getAccountStatus()).isEqualTo(DraftAccountStatus.PUBLISHING_FAILED);
        assertThat(entity.get().getAccountId()).isNull();
    }

    private void assertSuccessfullAcknowledgementsMessage(long vpfpoRegId, long defendantId) {
        AcknowledgementsQueueMessage msg = acknowledgementsTestPublisher.getNextMessage();
        assertThat(msg).extracting(
            AcknowledgementsQueueMessage::defendant_id,
            AcknowledgementsQueueMessage::registrationId,
            AcknowledgementsQueueMessage::status
        ).containsExactly(defendantId, vpfpoRegId, Status.PUBLISHED);
    }

    private void assertFailedAcknowledgementsMessage(long vpfpoRegId) {
        AcknowledgementsQueueMessage msg = acknowledgementsTestPublisher.getNextMessage();
        assertThat(msg).extracting(
            AcknowledgementsQueueMessage::defendant_id,
            AcknowledgementsQueueMessage::registrationId,
            AcknowledgementsQueueMessage::status
        ).containsExactly(null, vpfpoRegId, Status.PUBLISHING_FAILED);
    }

    private void assertNoMoreAcknowledgementsMessages() {
        AcknowledgementsQueueMessage msg = acknowledgementsTestPublisher.getNextMessage();
        assertThat(msg).isNull();
    }

    private void queueMessage(PublishDraftAccountQueueMessage message) throws JMSException {
        String payload = toJsonString(message);
        TextMessage textMessage = new AmqpJmsTextMessageFacade().asJmsMessage();
        textMessage.setText(payload);
        listener.onMessage(textMessage);
    }

    private PublishDraftAccountQueueMessage publishDraftAccountQueueMessage(long id) {
        return new PublishDraftAccountQueueMessage(id);
    }
}