package uk.gov.hmcts.opal.service.refdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.service.refdata.EmulatorUtil.CONNECTION_STRING;
import static uk.gov.hmcts.opal.service.refdata.EmulatorUtil.SUBSCRIPTION;
import static uk.gov.hmcts.opal.service.refdata.EmulatorUtil.TOPIC;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaType;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.util.FeatureFlags;

@Transactional
@DisplayName("Ref Data Queue Consumer Integration Tests")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RefDataMessagingIntegrationTest extends AbstractIntegrationTest {

    private final LocalJusticeAreaRepository localJusticeAreaRepository;

    @Autowired
    EmulatorUtil emulatorUtil;

    @DynamicPropertySource
    static void refDataServiceBusProperties(DynamicPropertyRegistry registry) {
        registry.add("opal.common.service-bus.connection-string",() -> CONNECTION_STRING);
        registry.add("opal.ref-data.service-bus.topic-name",() -> TOPIC);
        registry.add("opal.ref-data.service-bus.subscription-name",() -> SUBSCRIPTION);
        registry.add("opal.common.service-bus.protocol",() -> "amqp");
        registry.add("opal.common.service-bus.idle-timeout-ms",() -> "9999");
        registry.add("opal.common.service-bus.send-timeout-ms",() -> "9999");
        registry.add("opal.ref-data.service-bus.consumer-enabled", () -> true);
        registry.add("opal.report.service-bus.consumer-enabled", () -> false);
    }

    @PersistenceContext
    private EntityManager entityManager;

    @MockitoBean
    private FeatureToggleApi featureToggleApi;

    @BeforeEach
    void enableRefDataMessageProcessing() {
        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
            FeatureFlags.REF_DATA_MESSAGE_PROCESSING,
            FeatureFlags.REF_DATA_MESSAGE_PROCESSING_ENABLED_PROPERTY,
            false
        )).thenReturn(true);
    }

    @Test
    void processMessage_updatesExistingLocalJusticeAreaFromPayload() {

        final Short localJusticeAreaId = (short)401;

        String payload = MessageBuilder.buildRefDataLjaMessage("LJA", 1, true, String.valueOf(localJusticeAreaId),
            "Updated LJA", "2027-03-04",
            "New address line 171",
            "New address line 2",
            "New address line 3",
            "New address line 4",
            "NE1 2BB");

        emulatorUtil.publishConfiguredMessage(payload,"ref-data-session-id");

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
                assertThat(updated.getPostcode()).isEqualTo("NE1 2BB");
                assertThat(updated.getEndDate()).isEqualTo(LocalDateTime.of(2027, 3, 4, 0, 0));
                assertThat(updated.getLjaType()).isEqualTo(LocalJusticeAreaType.LJA);
                assertThat(updated.getLjaCode()).isEqualTo(null);
            });

        //no messages on queue now
        assertThat(emulatorUtil.getMessagesLeft()).isEmpty();
    }

}
