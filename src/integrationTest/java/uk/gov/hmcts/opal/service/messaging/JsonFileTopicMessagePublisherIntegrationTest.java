package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.util.FeatureFlags;

@EnabledIfEnvironmentVariable(named = "REF_DATA_TOPIC_IT_ENABLED", matches = "true")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class JsonFileTopicMessagePublisherIntegrationTest extends AbstractIntegrationTest {
    private static final Duration PROCESSING_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);
    private static final String EXPECTED_LJA_NAME = "Example Local Justice Area";
    private static final String EXPECTED_ADDRESS_LINE_1 = "123 Example Street";
    private static final String EXPECTED_ADDRESS_LINE_2 = "Example Town";
    private static final String EXPECTED_POSTCODE = "EX1 2AB";
    private static final short EXISTING_LOCAL_JUSTICE_AREA_ID = 9999;

    private final LocalJusticeAreaRepository localJusticeAreaRepository;

    @MockitoBean
    private FeatureToggleApi featureToggleApi;

    JsonFileTopicMessagePublisherIntegrationTest(LocalJusticeAreaRepository localJusticeAreaRepository) {
        this.localJusticeAreaRepository = localJusticeAreaRepository;
    }

    @DynamicPropertySource
    static void refDataServiceBusProperties(DynamicPropertyRegistry registry) {
        registry.add("opal.common.service-bus.connection-string", () -> publisherConfig().connectionString());
        registry.add("opal.common.service-bus.protocol", () -> publisherConfig().protocol());
        registry.add("opal.ref-data.service-bus.topic-name", () -> publisherConfig().topicName());
        registry.add("opal.ref-data.service-bus.subscription-name", () -> publisherConfig().subscriptionName());
        registry.add("opal.ref-data.service-bus.consumer-enabled", () -> true);
        registry.add("opal.report.service-bus.consumer-enabled", () -> false);
    }

    @BeforeEach
    void enableRefDataMessageProcessing() {
        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
            FeatureFlags.REF_DATA_MESSAGE_PROCESSING,
            FeatureFlags.REF_DATA_MESSAGE_PROCESSING_ENABLED_PROPERTY,
            false
        )).thenReturn(true);
    }

    @Test
    void publishAndProcessJsonMessageThroughRefDataTopicListener() {
        prepareExistingLocalJusticeAreaForUpdate();
        JsonFileTopicMessagePublisher.PublisherConfig publisherConfig = publisherConfig();

        new JsonFileTopicMessagePublisher(publisherConfig).publishConfiguredMessage();

        LocalJusticeAreaEntity localJusticeArea = awaitProcessedLocalJusticeArea();

        assertThat(localJusticeArea.getLocalJusticeAreaId()).isEqualTo(EXISTING_LOCAL_JUSTICE_AREA_ID);
        assertThat(localJusticeArea.getName()).isEqualTo(EXPECTED_LJA_NAME);
        assertThat(localJusticeArea.getAddressLine1()).isEqualTo(EXPECTED_ADDRESS_LINE_1);
        assertThat(localJusticeArea.getAddressLine2()).isEqualTo(EXPECTED_ADDRESS_LINE_2);
        assertThat(localJusticeArea.getAddressLine3()).isNull();
        assertThat(localJusticeArea.getAddressLine4()).isNull();
        assertThat(localJusticeArea.getPostcode()).isEqualTo(EXPECTED_POSTCODE);
        assertThat(localJusticeArea.getEndDate()).isNull();
    }

    private static JsonFileTopicMessagePublisher.PublisherConfig publisherConfig() {
        return JsonFileTopicMessagePublisher.loadConfig();
    }

    private void prepareExistingLocalJusticeAreaForUpdate() {
        jdbcTemplate.update("""
                update local_justice_areas
                set name = ?
                where local_justice_area_id = ?
                """, "Pending JMS update", EXISTING_LOCAL_JUSTICE_AREA_ID);
    }

    private LocalJusticeAreaEntity awaitProcessedLocalJusticeArea() {
        Instant deadline = Instant.now().plus(PROCESSING_TIMEOUT);
        while (Instant.now().isBefore(deadline)) {
            var localJusticeArea = localJusticeAreaRepository.findById(EXISTING_LOCAL_JUSTICE_AREA_ID);
            if (localJusticeArea.filter(this::hasExpectedMessageValues).isPresent()) {
                return localJusticeArea.get();
            }
            sleepBeforeRetry();
        }
        fail("Timed out waiting for ref-data JMS listener to process LJA message with id %s", EXISTING_LOCAL_JUSTICE_AREA_ID);
        return null;
    }

    private boolean hasExpectedMessageValues(LocalJusticeAreaEntity localJusticeArea) {
        return Objects.equals(EXPECTED_LJA_NAME, localJusticeArea.getName())
            && Objects.equals(EXPECTED_ADDRESS_LINE_1, localJusticeArea.getAddressLine1())
            && Objects.equals(EXPECTED_ADDRESS_LINE_2, localJusticeArea.getAddressLine2())
            && Objects.equals(EXPECTED_POSTCODE, localJusticeArea.getPostcode());
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(POLL_INTERVAL.toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for ref-data JMS listener", ex);
        }
    }

}
