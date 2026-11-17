package uk.gov.hmcts.opal.service.refdata.prison;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.refdata.RefDataServiceBusSessionPuller;
import uk.gov.hmcts.opal.refdata.schemavalidation.JsonSchemaValidationException;
import uk.gov.hmcts.opal.repository.PrisonRepository;
import uk.gov.hmcts.opal.service.refdata.EmulatorUtil;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@EnabledIfEnvironmentVariable(named = "REF_DATA_SERVICE_BUS_EMULATOR_TEST_ENABLED", matches = "true")
@RequiredArgsConstructor
@JiraStory("PO-11022")
@JiraEpic("PO-10769")
class PrisonMessagingIntegrationTest extends AbstractIntegrationTest {

    private static final String SESSION_ID = "prison-ref-data-session-id";
    private static final String CONNECTION_STRING =
        "Endpoint=sb://localhost;SharedAccessKeyName=RootManageSharedAccessKey;"
            + "SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;";

    private final PrisonRepository repository;
    private final RefDataServiceBusSessionPuller puller;

    @PersistenceContext
    private EntityManager entityManager;

    private EmulatorUtil emulatorUtil;

    @DynamicPropertySource
    static void refDataServiceBusProperties(DynamicPropertyRegistry registry) {
        registry.add("opal.ref-data.service-bus.consumer-enabled", () -> true);
        registry.add("opal.report.service-bus.consumer-enabled", () -> false);
        registry.add("opal.common.service-bus.connection-string", () -> CONNECTION_STRING);
        registry.add("opal.ref-data.service-bus.topic-name", () -> TOPIC);
        registry.add("opal.ref-data.service-bus.subscription-name", () -> SUBSCRIPTION);
        registry.add("opal.ref-data.service-bus.initialDelaySeconds", () -> 300);
        registry.add("opal.ref-data.service-bus.fixedDelaySeconds", () -> 300);
        registry.add("opal.ref-data.service-bus.receiveTimeoutSeconds", () -> 3);
        registry.add("opal.ref-data.service-bus.maxMessagesPerSession", () -> 50);
    }

    @BeforeEach
    void setUp() {
        emulatorUtil = new EmulatorUtil(CONNECTION_STRING);
    }

    @AfterEach
    void cleanUpMessages() {
        try (EmulatorUtil client = emulatorUtil) {
            client.clearSessionMessages(SESSION_ID);
        }
    }

    @Test
    void processesMessageAndUpdatesExistingPrison() {
        // Given
        String name = "Messaging Existing Prison";
        repository.saveAndFlush(PrisonEntity.builder().name(name).addressLine1("Old line").build());
        String payload = PrisonMessageBuilder.message(name, "PRIS", "Updated prison line", "PR5 5EE");

        // When
        publishAndWait(payload);
        puller.pullSessionMessages();

        // Then
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            entityManager.clear();
            PrisonEntity updated = repository.findByName(name).orElseThrow();
            assertThat(updated.getAddressLine1()).isEqualTo("Updated prison line");
            assertThat(updated.getPostcode()).isEqualTo("PR5 5EE");
        });
        repository.deleteByName(name);
    }

    @Test
    void processesMessageAndInsertsNewPrison() {
        // Given
        String name = "Messaging New Prison";
        String payload = PrisonMessageBuilder.validMessage(name);

        // When
        publishAndWait(payload);
        puller.pullSessionMessages();

        // Then
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            entityManager.clear();
            assertThat(repository.findByName(name)).isPresent();
        });
        repository.deleteByName(name);
    }

    @Test
    void stopsProcessingSessionWhenPayloadIsInvalid() {
        // Given
        String processedName = "Messaging Processed Prison";
        String unprocessedName = "Messaging Unprocessed Prison";
        String processedPayload = PrisonMessageBuilder.validMessage(processedName);
        String invalidPayload = "{}";
        String unprocessedPayload = PrisonMessageBuilder.validMessage(unprocessedName);

        // When / Then
        publishAndWait(processedPayload, invalidPayload, unprocessedPayload);
        assertThatThrownBy(puller::pullSessionMessages)
            .isInstanceOf(JsonSchemaValidationException.class);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            entityManager.clear();
            assertThat(repository.findByName(processedName)).isPresent();
            assertThat(repository.findByName(unprocessedName)).isEmpty();
        });
        repository.deleteByName(processedName);
    }

    private void publishAndWait(String... payloads) {
        for (String payload : payloads) {
            emulatorUtil.publishMessageToTopic(payload, SESSION_ID);
        }
        await().atMost(Duration.ofSeconds(20)).untilAsserted(() ->
            assertThat(emulatorUtil.peekQueueMessages()).containsExactly(payloads));
    }
}
