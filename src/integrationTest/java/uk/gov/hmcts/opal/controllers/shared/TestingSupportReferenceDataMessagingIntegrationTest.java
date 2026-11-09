package uk.gov.hmcts.opal.controllers.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.service.refdata.EmulatorUtil.SUBSCRIPTION;
import static uk.gov.hmcts.opal.service.refdata.EmulatorUtil.TOPIC;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.refdata.RefDataServiceBusSessionPuller;
import uk.gov.hmcts.opal.service.refdata.EmulatorUtil;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@EnabledIfEnvironmentVariable(named = "REF_DATA_SERVICE_BUS_EMULATOR_TEST_ENABLED", matches = "true")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class TestingSupportReferenceDataMessagingIntegrationTest extends AbstractIntegrationTest {

    private static final String CONNECTION_STRING =
        "Endpoint=sb://localhost;SharedAccessKeyName=RootManageSharedAccessKey;"
            + "SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;";
    private static final short LJA_ID = 9982;
    private static final String SESSION_ID = "ref-data-session-id";
    private static final String URL_BASE = "/testing-support/ref-data";
    private static final String MESSAGE = """
        {
          "header": {
            "message_id": "4d71a80b-538b-4f21-a64a-25fa00be66f3",
            "message_type": "ReferenceData",
            "data_product": "LJA",
            "operation": "PUBLISH",
            "source_system": "Semarchy",
            "created_date_time": "2026-01-01T00:00:00Z",
            "release_package_id": 1,
            "record_count": 1
          },
          "payload": {
            "records": [{
              "lja_code": "9982",
              "lja_name": "Messaging test LJA",
              "lja_type": "CRWCRT",
              "start_date": "2026-01-01",
              "end_date": null,
              "publishing_status": "Active",
              "cja_code": "41",
              "addresses": [{
                "address_type": "Court Address",
                "address_line_1": "Test address",
                "postcode": "GIR 0AA"
              }],
              "contact_information": []
            }]
          }
        }
        """;

    private final MockMvc mockMvc;
    private final LocalJusticeAreaRepository localJusticeAreaRepository;

    @Autowired
    private RefDataServiceBusSessionPuller puller;

    private EmulatorUtil emulatorUtil;

    @MockitoBean
    private FeatureToggleApi featureToggleApi;

    @DynamicPropertySource
    static void serviceBusProperties(DynamicPropertyRegistry registry) {
        registry.add("opal.common.service-bus.connection-string", () -> CONNECTION_STRING);
        registry.add("opal.ref-data.service-bus.consumer-enabled", () -> true);
        registry.add("opal.report.service-bus.consumer-enabled", () -> false);
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
        localJusticeAreaRepository.deleteById(LJA_ID);
        localJusticeAreaRepository.flush();
    }

    @AfterEach
    void cleanUp() {
        localJusticeAreaRepository.deleteById(LJA_ID);
        localJusticeAreaRepository.flush();
        try (EmulatorUtil client = emulatorUtil) {
            client.clearSessionMessages(SESSION_ID);
        }
    }

    @Test
    @JiraStory("PO-10705")
    @JiraEpic("PO-9031")
    void publishedReferenceDataCanBeDeletedByBusinessKey() throws Exception {

        // Use test support endoint to publish the messsage - a new LJA
        mockMvc.perform(post(URL_BASE + "/topic")
                .contentType("application/json")
                .content(MESSAGE))
            .andExpect(status().isAccepted());

        // Wait for the message to appear on the subscriber queue
        await().atMost(Duration.ofSeconds(20))
            .untilAsserted(() -> {
                var queuedMessages = emulatorUtil.peekQueueMessages();
                assertThat(queuedMessages).hasSize(1);
                assertThat(objectMapper.readTree(queuedMessages.getFirst()))
                    .isEqualTo(objectMapper.readTree(MESSAGE));
            });

        // Pull down the message
        puller.pullSessionMessages();

        // Wait the message LJA to be created
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            LocalJusticeAreaEntity inserted = localJusticeAreaRepository.findById(LJA_ID).orElseThrow();
            assertThat(inserted.getName()).isEqualTo("Messaging test LJA");
        });

        // Use test support endoint to delete this new LJA
        mockMvc.perform(delete(URL_BASE + "/LJA/9982"))
            .andExpect(status().isNoContent());

        // Check the new LJA has gone from the database
        assertThat(localJusticeAreaRepository.findById(LJA_ID)).isEmpty();
    }
}
