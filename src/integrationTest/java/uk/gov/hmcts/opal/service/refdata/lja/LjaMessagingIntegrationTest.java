package uk.gov.hmcts.opal.service.refdata.lja;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static uk.gov.hmcts.opal.service.refdata.EmulatorUtil.SUBSCRIPTION;
import static uk.gov.hmcts.opal.service.refdata.EmulatorUtil.TOPIC;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaType;
import uk.gov.hmcts.opal.refdata.RefDataServiceBusSessionPuller;
import uk.gov.hmcts.opal.refdata.schemavalidation.JsonSchemaValidationException;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.service.refdata.EmulatorUtil;

/**
 * Requires the Service Bus emulator supplied by the shared infrastructure project.
 *
 * <p>
 * Run explicitly with {@code REF_DATA_SERVICE_BUS_EMULATOR_TEST_ENABLED=true} after the
 * shared infrastructure emulator is running. The test is disabled by default because it
 * connects to the externally managed emulator rather than starting its own broker.
 */
@DisplayName("LJA Ref Data Messaging Integration Tests")
@EnabledIfEnvironmentVariable(named = "REF_DATA_SERVICE_BUS_EMULATOR_TEST_ENABLED", matches = "true")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class LjaMessagingIntegrationTest extends AbstractIntegrationTest {

    public static final String SESSION_ID = "ref-data-session-id";
    private static final String CONNECTION_STRING =
        "Endpoint=sb://localhost;SharedAccessKeyName=RootManageSharedAccessKey;"
            + "SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;";
    private final LocalJusticeAreaRepository localJusticeAreaRepository;

    private EmulatorUtil emulatorUtil;

    @Autowired
    RefDataServiceBusSessionPuller puller;

    @DynamicPropertySource
    static void refDataServiceBusProperties(DynamicPropertyRegistry registry) {

        registry.add("opal.ref-data.service-bus.consumer-enabled", () -> true);
        registry.add("opal.report.service-bus.consumer-enabled", () -> false);

        registry.add("opal.common.service-bus.connection-string",() -> CONNECTION_STRING);

        registry.add("opal.ref-data.service-bus.topic-name",() -> TOPIC);
        registry.add("opal.ref-data.service-bus.subscription-name",() -> SUBSCRIPTION);
        registry.add("opal.ref-data.service-bus.initialDelaySeconds",() -> 300);
        registry.add("opal.ref-data.service-bus.fixedDelaySeconds",() -> 300);
        registry.add("opal.ref-data.service-bus.receiveTimeoutSeconds",() -> 3);
        registry.add("opal.ref-data.service-bus.maxMessagesPerSession",() -> 50);
    }

    @PersistenceContext
    private EntityManager entityManager;

    @MockitoBean
    private FeatureToggleApi featureToggleApi;

    @BeforeEach
    void setUp() {
        emulatorUtil = new EmulatorUtil(CONNECTION_STRING);
    }

    @AfterEach
    void cleanUpMessages() {
        //The try-with-resources ensure emulatorUtil.close() is called afterward,
        //even if clearing the messages throws an exception
        try (EmulatorUtil client = emulatorUtil) {
            client.clearSessionMessages(SESSION_ID);
        }
    }

    @JiraTestKey("PO-10709")
    @JiraStory("PO-10709")
    @JiraEpic("PO-9031")
    @Test
    // AC1: A valid LJA message updates an existing local justice area.
    void processMessage_updatesExistingLocalJusticeAreaFromPayload() {

        final Short localJusticeAreaId = (short)401;

        String payload = LjaMessageBuilder.buildRefDataLjaMessage("LJA", 1, true,
            String.format("%04d", localJusticeAreaId),
            "Updated LJA", "2027-03-04",
            "New address line 171",
            "New address line 2",
            "New address line 3",
            "New address line 4",
            "New address line 5",
            "NE1 2BB");

        publishAndWait(payload);

        puller.pullSessionMessages();

        //check db looks good
        await()
            .atMost(Duration.ofSeconds(10))
            .pollInterval(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                entityManager.clear();
                var updated = localJusticeAreaRepository.findById(localJusticeAreaId).get();
                assertThat(updated.getName()).isEqualTo("Updated LJA");
                assertThat(updated.getAddressLine1()).isEqualTo("New address line 171");
                assertThat(updated.getAddressLine2()).isEqualTo("New address line 2");
                assertThat(updated.getAddressLine3()).isEqualTo("New address line 3");
                assertThat(updated.getAddressLine4()).isEqualTo("New address line 4");
                assertThat(updated.getAddressLine5()).isEqualTo("New address line 5");
                assertThat(updated.getPostcode()).isEqualTo("NE1 2BB");
                assertThat(updated.getEndDate()).isNull();
                assertThat(updated.getLjaType()).isEqualTo(LocalJusticeAreaType.CRWCRT);
            });

        //check queues looks good
        await().atMost(Duration.ofSeconds(100))
            .untilAsserted(() -> {
                assertThat(emulatorUtil.peekQueueMessages()).isEmpty();
                assertThat(emulatorUtil.peekDlqMessages()).isEmpty();
            });
    }

    @JiraTestKey("PO-10709")
    @JiraStory("PO-10709")
    @JiraEpic("PO-9031")
    @Test
    // AC1: A valid LJA message inserts a new local justice area.
    void processMessage_insertsNewLocalJusticeAreaFromPayload() {

        final Short localJusticeAreaId = (short)9998;

        assertThat(localJusticeAreaRepository.findById(localJusticeAreaId)).isEmpty();

        String payload = LjaMessageBuilder.buildRefDataLjaMessage("LJA", 1, true,
            String.format("%04d", localJusticeAreaId), "Inserted LJA", null,
            "Inserted address line 1", "Inserted address line 2", "Inserted address line 3",
            "Inserted address line 4", "Inserted address line 5", "IN1 2BB");

        publishAndWait(payload);

        puller.pullSessionMessages();

        await()
            .atMost(Duration.ofSeconds(10))
            .pollInterval(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                entityManager.clear();
                var inserted = localJusticeAreaRepository.findById(localJusticeAreaId).orElseThrow();
                assertThat(inserted.getName()).isEqualTo("Inserted LJA");
                assertThat(inserted.getAddressLine1()).isEqualTo("Inserted address line 1");
                assertThat(inserted.getAddressLine2()).isEqualTo("Inserted address line 2");
                assertThat(inserted.getAddressLine3()).isEqualTo("Inserted address line 3");
                assertThat(inserted.getAddressLine4()).isEqualTo("Inserted address line 4");
                assertThat(inserted.getAddressLine5()).isEqualTo("Inserted address line 5");
                assertThat(inserted.getPostcode()).isEqualTo("IN1 2BB");
                assertThat(inserted.getLjaType()).isEqualTo(LocalJusticeAreaType.CRWCRT);
                assertThat(inserted.getEndDate()).isNull();
            });

        await().atMost(Duration.ofSeconds(100))
            .untilAsserted(() -> {
                assertThat(emulatorUtil.peekQueueMessages()).isEmpty();
                assertThat(emulatorUtil.peekDlqMessages()).isEmpty();
            });
    }

    @JiraTestKey("PO-10709")
    @JiraStory("PO-10709")
    @JiraEpic("PO-9031")
    @Test
    // AC2: A valid unsupported reference-data type is consumed without changing LJA data.
    void processMessage_ignoresValidUnsupportedReferenceDataType() {

        final Short localJusticeAreaId = (short)9997;
        String payload = LjaMessageBuilder.validUnsupportedReferenceDataMessage();

        assertThat(localJusticeAreaRepository.findById(localJusticeAreaId)).isEmpty();

        publishAndWait(payload);

        puller.pullSessionMessages();

        await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                entityManager.clear();
                assertThat(localJusticeAreaRepository.findById(localJusticeAreaId)).isEmpty();
            });

        await().atMost(Duration.ofSeconds(100))
            .untilAsserted(() -> {
                assertThat(emulatorUtil.peekQueueMessages()).isEmpty();
                assertThat(emulatorUtil.peekDlqMessages()).isEmpty();
            });
    }

    @JiraTestKey("PO-10709")
    @JiraStory("PO-10709")
    @JiraEpic("PO-9031")
    @Test
    // AC3: A failed record rolls back all updates from the same message.
    void processMessage_rollsBackAllUpdatesWhenRecordProcessingFails() {

        final Short firstLocalJusticeAreaId = (short)9996;
        final Short failingLocalJusticeAreaId = (short)9995;
        String payload = LjaMessageBuilder.messageWithProcessingFailure(
            firstLocalJusticeAreaId, failingLocalJusticeAreaId);

        assertThat(localJusticeAreaRepository.findById(firstLocalJusticeAreaId)).isEmpty();
        assertThat(localJusticeAreaRepository.findById(failingLocalJusticeAreaId)).isEmpty();

        publishAndWait(payload);

        assertThatThrownBy(puller::pullSessionMessages)
            .isInstanceOf(RuntimeException.class);

        await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                entityManager.clear();
                assertThat(localJusticeAreaRepository.findById(firstLocalJusticeAreaId)).isEmpty();
                assertThat(localJusticeAreaRepository.findById(failingLocalJusticeAreaId)).isEmpty();
            });

        await().atMost(Duration.ofSeconds(100))
            .untilAsserted(() -> {
                assertThat(emulatorUtil.peekQueueMessages()).containsExactly(payload);
                assertThat(emulatorUtil.peekDlqMessages()).isEmpty();
            });
    }

    @JiraTestKey("PO-10709")
    @JiraStory("PO-10709")
    @JiraEpic("PO-9031")
    @Test
    // AC4: A repeatedly failing message remains at the front of the queue and is never dead-lettered.
    void processMessage_keepsFailingMessageAtFrontOfQueueAcrossRetries() {

        String payload = LjaMessageBuilder.messageWithProcessingFailure((short)9994, (short)9993);
        String followingPayload = LjaMessageBuilder.buildRefDataLjaMessage("LJA", 1, true,
            "9992", "Following LJA", null,
            "Following address line 1", "Following address line 2", "Following address line 3",
            "Following address line 4", "Following address line 5", "IN2 2BB");

        publishAndWait(payload, followingPayload);

        for (int attempt = 1; attempt <= 3; attempt++) {
            assertThatThrownBy(puller::pullSessionMessages)
                .isInstanceOf(RuntimeException.class);

            await()
                .atMost(Duration.ofSeconds(100))
                .untilAsserted(() -> {
                    assertThat(emulatorUtil.peekQueueMessages()).containsExactly(payload, followingPayload);
                    assertThat(emulatorUtil.peekDlqMessages()).isEmpty();
                });
        }
    }

    @JiraTestKey("PO-10709")
    @JiraStory("PO-10709")
    @JiraEpic("PO-9031")
    @Test
    // AC5: A malformed JSON message remains queued and is not dead-lettered.
    void processMessage_leavesInvalidPayloadOnQueueAndNotInDlq() {

        String payload = "{xx}";

        publishAndWait(payload);
        assertThatThrownBy(puller::pullSessionMessages)
            .isInstanceOf(IllegalArgumentException.class);

        await().atMost(Duration.ofSeconds(100))
            .untilAsserted(() -> {
                assertThat(emulatorUtil.peekQueueMessages()).containsExactly(payload);
                assertThat(emulatorUtil.peekDlqMessages()).isEmpty();
            });

    }

    @JiraTestKey("PO-10709")
    @JiraStory("PO-10709")
    @JiraEpic("PO-9031")
    @Test
    // AC4: A failed message remains queued and is not dead-lettered.
    // AC5: A schema-invalid message stops session processing and remains queued.
    void processMessage_stopsProcessingSessionWhenMiddlePayloadIsInvalid() {

        final Short localJusticeAreaId = (short)402;
        final Short unprocessedLocalJusticeAreaId = (short)403;

        String processedPayload = LjaMessageBuilder.buildRefDataLjaMessage("LJA", 1, true,
            String.format("%04d", localJusticeAreaId), "Updated LJA before invalid payload", "2027-03-04",
            "New address line before invalid",
            "New address line 2",
            "New address line 3",
            "New address line 4",
            "New address line 5",
            "NE1 2BB");
        String invalidPayload = "{}";
        String unprocessedPayload = LjaMessageBuilder.buildRefDataLjaMessage("LJA", 1, true,
            String.format("%04d", unprocessedLocalJusticeAreaId),
            "Updated LJA after invalid payload", "2027-03-04",
            "New address line after invalid",
            "New address line 2",
            "New address line 3",
            "New address line 4",
            "New address line 5",
            "NE1 2BB");

        publishAndWait(processedPayload, invalidPayload, unprocessedPayload);

        assertThatThrownBy(puller::pullSessionMessages)
            .isInstanceOf(JsonSchemaValidationException.class);

        //check db
        await()
            .atMost(Duration.ofSeconds(10))
            .pollInterval(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                entityManager.clear();
                var updated = localJusticeAreaRepository.findById(localJusticeAreaId).get();
                assertThat(updated.getName()).isEqualTo("Updated LJA before invalid payload");
                assertThat(updated.getAddressLine1()).isEqualTo("New address line before invalid");
            });

        //check queues
        await()
            .atMost(Duration.ofSeconds(100))
            .pollInterval(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(emulatorUtil.peekQueueMessages()).containsExactly(invalidPayload, unprocessedPayload);
                assertThat(emulatorUtil.peekDlqMessages()).isEmpty();
            });
    }

    private void publishAndWait(String... payloads) {
        for (String payload : payloads) {
            emulatorUtil.publishMessageToTopic(payload, SESSION_ID);
        }
        await().atMost(Duration.ofSeconds(20)).untilAsserted(() ->
            assertThat(emulatorUtil.peekQueueMessages()).containsExactly(payloads));
    }
}
