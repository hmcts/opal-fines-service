package uk.gov.hmcts.opal.service.refdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaType;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.service.refdata.framework.RefDataMessageProcessor;
import uk.gov.hmcts.opal.util.FeatureFlags;

@Transactional
@DisplayName("Ref Data Queue Consumer Integration Tests")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RefDataMessageProcessorIntegrationTest extends AbstractIntegrationTest {

    private static final String REF_DATA_MESSAGE_SCHEMA = "ref-data/ref_data_schema.json";

    private final RefDataMessageProcessor consumer;
    private final LocalJusticeAreaRepository localJusticeAreaRepository;
    private final PlatformTransactionManager transactionManager;

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
        LocalJusticeAreaEntity original = localJusticeAreaRepository.findAll().stream()
            .findFirst()
            .orElseThrow();

        final Short localJusticeAreaId = original.getLocalJusticeAreaId();

        localJusticeAreaRepository.saveAndFlush(original);
        entityManager.flush();
        entityManager.clear();

        consumer.processMessage(buildRefDataLjaMessage("LJA", 1, true, String.valueOf(localJusticeAreaId),
            "Updated LJA", "2027-03-04",
            "New address line 1", "New address line 2", "New address line 3", "New address line 4",
            "NE1 2BB"));

        entityManager.flush();
        entityManager.clear();

        LocalJusticeAreaEntity updated = localJusticeAreaRepository.findById(localJusticeAreaId).orElseThrow();

        assertThat(updated.getLocalJusticeAreaId()).isEqualTo(localJusticeAreaId);
        assertThat(updated.getLjaCode()).isEqualTo(null);
        assertThat(updated.getName()).isEqualTo("Updated LJA");
        assertThat(updated.getAddressLine1()).isEqualTo("New address line 1");
        assertThat(updated.getAddressLine2()).isEqualTo("New address line 2");
        assertThat(updated.getAddressLine3()).isEqualTo("New address line 3");
        assertThat(updated.getAddressLine4()).isEqualTo("New address line 4");
        assertThat(updated.getPostcode()).isEqualTo("NE1 2BB");
        assertThat(updated.getEndDate()).isEqualTo(LocalDateTime.of(2027, 3, 4, 0, 0));
        assertThat(updated.getLjaType()).isEqualTo(LocalJusticeAreaType.LJA);
    }

    @Test
    void processMessage_createsNewLocalJusticeAreaFromPayload() {

        String ljaCode = "124";
        final long beforeCount = localJusticeAreaRepository.count();

        consumer.processMessage(buildRefDataLjaMessage("LJA", 1, true, ljaCode, "Created LJA", "2027-03-04",
            "New address line 1", "New address line 2", "New address line 3", "New address line 4",
            "NE1 2BB"));

        entityManager.flush();
        entityManager.clear();

        LocalJusticeAreaEntity created = localJusticeAreaRepository.findById(Short.valueOf(ljaCode)).orElseThrow();

        assertThat(localJusticeAreaRepository.count()).isEqualTo(beforeCount + 1);
        assertThat(created.getLocalJusticeAreaId()).isNotNull();
        assertThat(created.getLjaCode()).isEqualTo(null);
        assertThat(created.getName()).isEqualTo("Created LJA");
        assertThat(created.getAddressLine1()).isEqualTo("New address line 1");
        assertThat(created.getAddressLine2()).isEqualTo("New address line 2");
        assertThat(created.getAddressLine3()).isEqualTo("New address line 3");
        assertThat(created.getAddressLine4()).isEqualTo("New address line 4");
        assertThat(created.getPostcode()).isEqualTo("NE1 2BB");
        assertThat(created.getEndDate()).isEqualTo(LocalDateTime.of(2027, 3, 4, 0, 0));
        assertThat(created.getLjaType()).isEqualTo(LocalJusticeAreaType.LJA);
    }

    @Test
    void processMessage_ignoresPayloadWhenRefDataMessageProcessingFeatureIsDisabled() {
        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
            FeatureFlags.REF_DATA_MESSAGE_PROCESSING,
            FeatureFlags.REF_DATA_MESSAGE_PROCESSING_ENABLED_PROPERTY,
            false
        )).thenReturn(false);

        LocalJusticeAreaEntity original = localJusticeAreaRepository.findAll().stream()
            .findFirst()
            .orElseThrow();
        String ljaCode = "Z129";
        final Short localJusticeAreaId = original.getLocalJusticeAreaId();
        final String originalName = original.getName();

        original.setLjaCode(ljaCode);
        localJusticeAreaRepository.saveAndFlush(original);
        entityManager.flush();
        entityManager.clear();

        consumer.processMessage(buildRefDataLjaMessage("LJA", 1, true, ljaCode, "Ignored LJA", "2027-03-04",
            "New address line 1", "New address line 2", "New address line 3", "New address line 4",
            "NE1 2BB"));

        entityManager.flush();
        entityManager.clear();

        LocalJusticeAreaEntity unchanged = localJusticeAreaRepository.findById(localJusticeAreaId).orElseThrow();

        assertThat(unchanged.getLjaCode()).isEqualTo(ljaCode);
        assertThat(unchanged.getName()).isEqualTo(originalName);
    }

    @Test
    void processMessage_rollsBackAllRecordsWhenAnyRecordFails() {
        TransactionTemplate requiresNewTransaction = new TransactionTemplate(transactionManager);
        requiresNewTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        Short localJusticeAreaId = requiresNewTransaction.execute(status -> {
            LocalJusticeAreaEntity localJusticeArea = localJusticeAreaRepository.findAll().stream()
                .filter(entity -> entity.getLjaCode() == null)
                .findFirst()
                .orElseThrow();

            localJusticeArea.setLjaCode("T001");
            localJusticeAreaRepository.saveAndFlush(localJusticeArea);
            return localJusticeArea.getLocalJusticeAreaId();
        });

        assertThat(localJusticeAreaId).isNotNull();

        entityManager.clear();

        LocalJusticeAreaEntity original = localJusticeAreaRepository.findById(localJusticeAreaId).orElseThrow();
        String originalName = original.getName();
        String originalLjaCode = original.getLjaCode();
        long beforeCount = localJusticeAreaRepository.count();

        entityManager.clear();

        ObjectMapper objectMapper = new ObjectMapper();
        String message = buildRefDataLjaMessage(
            objectMapper,
            "LJA",
            2,
            buildLjaRecordNode(objectMapper, true, "T001", "Rollback LJA", "2027-03-04", "New address line 1",
                "New address line 2", "New address line 3", "New address line 4", "NE1 2BB"),
            buildLjaRecordNode(objectMapper, true, "TOO-LONG", "Broken LJA", "2027-03-04", "New address line 1",
                "New address line 2", "New address line 3", "New address line 4", "NE1 2BB")
        );

        try {
            assertThatThrownBy(() -> requiresNewTransaction.execute(status -> {
                consumer.processMessage(message);
                return null;
            }))
                .isInstanceOf(RuntimeException.class);

            entityManager.clear();

            LocalJusticeAreaEntity reloaded = localJusticeAreaRepository.findById(localJusticeAreaId).orElseThrow();

            assertThat(localJusticeAreaRepository.count()).isEqualTo(beforeCount);
            assertThat(reloaded.getName()).isEqualTo(originalName);
            assertThat(reloaded.getLjaCode()).isEqualTo(originalLjaCode);
        } finally {
            requiresNewTransaction.execute(status -> {
                LocalJusticeAreaEntity localJusticeArea = localJusticeAreaRepository.findById(localJusticeAreaId)
                    .orElseThrow();
                localJusticeArea.setLjaCode(null);
                localJusticeAreaRepository.saveAndFlush(localJusticeArea);
                return null;
            });
            entityManager.clear();
        }
    }

    @Test
    void processMessage_throwsSchemaValidationExceptionWhenRequiredFieldIsMissing() {
        LocalJusticeAreaEntity original = localJusticeAreaRepository.findAll().stream()
            .findFirst()
            .orElseThrow();
        final Short localJusticeAreaId = original.getLocalJusticeAreaId();
        final String originalName = original.getName();
        final String ljaCode = original.getLjaCode() == null ? "Z125" : original.getLjaCode();
        final long beforeCount = localJusticeAreaRepository.count();

        assertThatThrownBy(() -> consumer.processMessage(buildRefDataLjaMessage("LJA", 1, false, ljaCode,
            "Updated LJA", "2027-03-04", "New address line 1", "New address line 2",
            "New address line 3", "New address line 4", "NE1 2BB")))
            .isInstanceOf(JsonSchemaValidationException.class)
            .hasMessageContaining(REF_DATA_MESSAGE_SCHEMA);

        entityManager.flush();
        entityManager.clear();

        assertThat(localJusticeAreaRepository.count()).isEqualTo(beforeCount);

        LocalJusticeAreaEntity reloaded = localJusticeAreaRepository.findById(localJusticeAreaId).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo(originalName);
        assertThat(reloaded.getLjaCode()).isEqualTo(original.getLjaCode());
    }

    @Test
    void processMessage_throwsSchemaValidationExceptionWhenRecordCountIsInvalid() {
        LocalJusticeAreaEntity original = localJusticeAreaRepository.findAll().stream()
            .findFirst()
            .orElseThrow();
        final Short localJusticeAreaId = original.getLocalJusticeAreaId();
        final String originalName = original.getName();
        final LocalJusticeAreaType originalType = original.getLjaType();
        final String ljaCode = original.getLjaCode() == null ? "Z126" : original.getLjaCode();
        final long beforeCount = localJusticeAreaRepository.count();

        if (original.getLjaCode() == null) {
            original.setLjaCode(ljaCode);
            localJusticeAreaRepository.saveAndFlush(original);
            entityManager.flush();
            entityManager.clear();
        }

        assertThatThrownBy(() -> consumer.processMessage(buildRefDataLjaMessage("LJA", 0, true, ljaCode,
            "Updated LJA", "2027-03-04", "New address line 1", "New address line 2", "New address line 3",
            "New address line 4", "NE1 2BB")))
            .isInstanceOf(JsonSchemaValidationException.class)
            .hasMessageContaining(REF_DATA_MESSAGE_SCHEMA);

        entityManager.flush();
        entityManager.clear();

        assertThat(localJusticeAreaRepository.count()).isEqualTo(beforeCount);

        LocalJusticeAreaEntity reloaded = localJusticeAreaRepository.findById(localJusticeAreaId).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo(originalName);
        assertThat(reloaded.getLjaCode()).isEqualTo(ljaCode);
        assertThat(reloaded.getLjaType()).isEqualTo(originalType);
    }

    @Test
    void processMessage_throwsSchemaValidationExceptionWhenUnknownRefDataTypeIsSupplied() {
        final long beforeCount = localJusticeAreaRepository.count();

        assertThatThrownBy(() -> consumer.processMessage(buildRefDataLjaMessage("UNKNOWN_REF_DATA_TYPE", 1, true,
            "Z127", "Unknown", "2027-03-04", "New address line 1", "New address line 2",
            "New address line 3", "New address line 4", "NE1 2BB")))
            .isInstanceOf(JsonSchemaValidationException.class)
            .hasMessageContaining(REF_DATA_MESSAGE_SCHEMA);

        entityManager.flush();
        entityManager.clear();

        assertThat(localJusticeAreaRepository.count()).isEqualTo(beforeCount);
    }

    @Test
    void processMessage_throwsIllegalArgumentExceptionWhenPayloadIsNotJson() {
        assertThatThrownBy(() -> consumer.processMessage("not-json"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unable to parse ref data message");
    }

    private String buildRefDataLjaMessage(String dataProduct, int recordCount, boolean includeLjaName, String ljaCode,
        String ljaName, String endDate, String addressLine1, String addressLine2, String addressLine3,
        String addressLine4, String postcode) {
        ObjectMapper objectMapper = new ObjectMapper();
        return buildRefDataLjaMessage(
            objectMapper,
            dataProduct,
            recordCount,
            buildLjaRecordNode(objectMapper, includeLjaName, ljaCode, ljaName, endDate, addressLine1, addressLine2,
                addressLine3, addressLine4, postcode)
        );
    }

    private String buildRefDataLjaMessage(String dataProduct, int recordCount, ObjectNode... recordNodes) {
        return buildRefDataLjaMessage(new ObjectMapper(), dataProduct, recordCount, recordNodes);
    }

    private String buildRefDataLjaMessage(ObjectMapper objectMapper, String dataProduct, int recordCount,
        ObjectNode... recordNodes) {
        try {
            ObjectNode rootNode = objectMapper.createObjectNode();
            ObjectNode headerNode = rootNode.putObject("header");
            headerNode.put("message_id", "437dacf6-511c-4e93-95f3-23e82b12e735");
            headerNode.put("message_type", "ReferenceData");
            headerNode.put("data_product", dataProduct);
            headerNode.put("operation", "PUBLISH");
            headerNode.put("source_system", "Semarchy");
            headerNode.put("created_date_time", "2026-09-02T08:28:56.935738+00:00");
            headerNode.put("release_package_id", 202);
            headerNode.put("record_count", recordCount);

            ObjectNode payloadNode = rootNode.putObject("payload");
            ArrayNode recordsNode = payloadNode.putArray("records");
            for (ObjectNode recordNode : recordNodes) {
                recordsNode.add(recordNode);
            }

            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to build ref-data test message", ex);
        }
    }

    private ObjectNode buildLjaRecordNode(ObjectMapper objectMapper, boolean includeLjaName, String ljaCode,
        String ljaName, String endDate, String addressLine1, String addressLine2, String addressLine3,
        String addressLine4, String postcode) {
        ObjectNode recordNode = objectMapper.createObjectNode();
        recordNode.put("lja_code", ljaCode);
        if (includeLjaName) {
            recordNode.put("lja_name", ljaName);
        }
        recordNode.put("end_date", endDate);
        recordNode.put("lja_type", "LJA");
        recordNode.put("start_date", "2027-03-01");

        ArrayNode addressesNode = recordNode.putArray("addresses");
        ObjectNode addressNode = addressesNode.addObject();
        addressNode.put("address_type", "Test Address");
        addressNode.put("address_line_1", addressLine1);
        addressNode.put("address_line_2", addressLine2);
        addressNode.put("address_line_3", addressLine3);
        addressNode.put("address_line_4", addressLine4);
        addressNode.put("post_code", postcode);

        ObjectNode secondaryAddressNode = addressesNode.addObject();
        secondaryAddressNode.put("address_type", "Secondary Address");
        secondaryAddressNode.put("address_line_1", "Secondary address line 1");
        secondaryAddressNode.put("post_code", "NE1 2BB");

        recordNode.putArray("contact_information");

        return recordNode;
    }
}
