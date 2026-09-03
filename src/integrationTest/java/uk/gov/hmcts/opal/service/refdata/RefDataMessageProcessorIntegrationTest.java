package uk.gov.hmcts.opal.service.refdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaType;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.service.refdata.framework.RefDataMessageProcessor;

@Transactional
@DisplayName("Ref Data Queue Consumer Integration Tests")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RefDataMessageProcessorIntegrationTest extends AbstractIntegrationTest {

    private static final String VALCON_REF_DATA_MESSAGE_SCHEMA = "ref-data/valcon_oneofpayload.json";

    private final RefDataMessageProcessor consumer;
    private final LocalJusticeAreaRepository localJusticeAreaRepository;
    private final PlatformTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void processMessage_updatesExistingLocalJusticeAreaFromPayload() {
        LocalJusticeAreaEntity original = localJusticeAreaRepository.findAll().stream()
            .findFirst()
            .orElseThrow();
        String ljaCode = "Z123";
        final Short localJusticeAreaId = original.getLocalJusticeAreaId();

        original.setLjaCode(ljaCode);
        localJusticeAreaRepository.saveAndFlush(original);
        entityManager.flush();
        entityManager.clear();

        consumer.processMessage(buildValconLjaMessage("LJA", 1, true, ljaCode, "Updated LJA", "2027-03-04",
            "New address line 1", "New address line 2", "New address line 3", "New address line 4",
            "NE1 2BB"));

        entityManager.flush();
        entityManager.clear();

        LocalJusticeAreaEntity updated = localJusticeAreaRepository.findByLjaCode(ljaCode).orElseThrow();

        assertThat(updated.getLocalJusticeAreaId()).isEqualTo(localJusticeAreaId);
        assertThat(updated.getLjaCode()).isEqualTo(ljaCode);
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
        Short nextLocalJusticeAreaId = jdbcTemplate.queryForObject("""
                select coalesce(max(local_justice_area_id), 0) + 1
                from local_justice_areas
                """, Short.class);
        jdbcTemplate.execute("""
                create sequence if not exists local_justice_area_id_seq
                start with %d increment by 1
                """.formatted(nextLocalJusticeAreaId));

        String ljaCode = "Z124";
        final long beforeCount = localJusticeAreaRepository.count();

        consumer.processMessage(buildValconLjaMessage("LJA", 1, true, ljaCode, "Created LJA", "2027-03-04",
            "New address line 1", "New address line 2", "New address line 3", "New address line 4",
            "NE1 2BB"));

        entityManager.flush();
        entityManager.clear();

        LocalJusticeAreaEntity created = localJusticeAreaRepository.findByLjaCode(ljaCode).orElseThrow();

        assertThat(localJusticeAreaRepository.count()).isEqualTo(beforeCount + 1);
        assertThat(created.getLocalJusticeAreaId()).isNotNull();
        assertThat(created.getLjaCode()).isEqualTo(ljaCode);
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
        String message = buildValconLjaMessage(
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

        assertThatThrownBy(() -> consumer.processMessage(buildValconLjaMessage("LJA", 1, false, ljaCode,
            "Updated LJA", "2027-03-04", "New address line 1", "New address line 2",
            "New address line 3", "New address line 4", "NE1 2BB")))
            .isInstanceOf(JsonSchemaValidationException.class)
            .hasMessageContaining(VALCON_REF_DATA_MESSAGE_SCHEMA);

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

        assertThatThrownBy(() -> consumer.processMessage(buildValconLjaMessage("LJA", 0, true, ljaCode,
            "Updated LJA", "2027-03-04", "New address line 1", "New address line 2", "New address line 3",
            "New address line 4", "NE1 2BB")))
            .isInstanceOf(JsonSchemaValidationException.class)
            .hasMessageContaining(VALCON_REF_DATA_MESSAGE_SCHEMA);

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

        assertThatThrownBy(() -> consumer.processMessage(buildValconLjaMessage("UNKNOWN_REF_DATA_TYPE", 1, true,
            "Z127", "Unknown", "2027-03-04", "New address line 1", "New address line 2",
            "New address line 3", "New address line 4", "NE1 2BB")))
            .isInstanceOf(JsonSchemaValidationException.class)
            .hasMessageContaining(VALCON_REF_DATA_MESSAGE_SCHEMA);

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

    private String buildValconLjaMessage(String dataProduct, int recordCount, boolean includeLjaCode, String ljaCode,
        String ljaName, String endDate, String addressLine1, String addressLine2, String addressLine3,
        String addressLine4, String postcode) {
        ObjectMapper objectMapper = new ObjectMapper();
        return buildValconLjaMessage(
            objectMapper,
            dataProduct,
            recordCount,
            buildLjaRecordNode(objectMapper, includeLjaCode, ljaCode, ljaName, endDate, addressLine1, addressLine2,
                addressLine3, addressLine4, postcode)
        );
    }

    private String buildValconLjaMessage(String dataProduct, int recordCount, ObjectNode... recordNodes) {
        return buildValconLjaMessage(new ObjectMapper(), dataProduct, recordCount, recordNodes);
    }

    private String buildValconLjaMessage(ObjectMapper objectMapper, String dataProduct, int recordCount,
        ObjectNode... recordNodes) {
        try {
            ObjectNode rootNode = objectMapper.createObjectNode();
            ObjectNode headerNode = rootNode.putObject("header");
            headerNode.put("messageId", "437dacf6-511c-4e93-95f3-23e82b12e735");
            headerNode.put("messageType", "ReferenceData");
            headerNode.put("dataProduct", dataProduct);
            headerNode.put("operation", "PUBLISH");
            headerNode.put("sourceSystem", "Semarchy");
            headerNode.put("createdDateTime", "2026-09-02T08:28:56.935738+00:00");
            headerNode.put("ReleasePackageId", 202);
            headerNode.put("recordCount", recordCount);

            ObjectNode payloadNode = rootNode.putObject("payload");
            ObjectNode recordsNode = payloadNode.putArray("records");
            for (ObjectNode recordNode : recordNodes) {
                recordsNode.add(recordNode);
            }

            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to build ref-data test message", ex);
        }
    }

    private ObjectNode buildLjaRecordNode(ObjectMapper objectMapper, boolean includeLjaCode, String ljaCode,
        String ljaName, String endDate, String addressLine1, String addressLine2, String addressLine3,
        String addressLine4, String postcode) {
        ObjectNode recordNode = objectMapper.createObjectNode();
        if (includeLjaCode) {
            recordNode.put("LJACode", ljaCode);
        }
        recordNode.put("LJAName", ljaName);
        recordNode.put("EndDate", endDate);
        recordNode.putNull("CourtWelshName");
        recordNode.putNull("CourtLocationCode");
        recordNode.put("StartDate", "2027-03-01");
        recordNode.putNull("EnforcementCode");
        recordNode.putNull("ClusterCode");
        recordNode.putNull("DivisionCode");
        recordNode.putNull("DefaultStartTime");
        recordNode.putNull("DefaultDuration");
        recordNode.putNull("CommonPlatformUUID");
        recordNode.putNull("Notes");
        recordNode.put("CourtHearingOperationAreaIndicator", false);
        recordNode.put("CrownCourtIndicator", false);
        recordNode.put("NorthernIrelandCourtIndicator", false);
        recordNode.put("MagistratesCourtIndicator", true);
        recordNode.put("ScottishDistrictCourtIndicator", false);
        recordNode.put("ScottishSheriffCourtIndicator", false);
        recordNode.put("ScottishJusticeOfPeaceCourtIndicator", false);
        recordNode.put("YouthCourtIndicator", false);

        ObjectNode addressNode = recordNode.putArray("Addresses").addObject();
        addressNode.put("AddressType", "Test Address");
        addressNode.put("AddressLine1", addressLine1);
        addressNode.put("AddressLine2", addressLine2);
        addressNode.put("AddressLine3", addressLine3);
        addressNode.put("AddressLine4", addressLine4);
        addressNode.put("Postcode", postcode);

        return recordNode;
    }
}
