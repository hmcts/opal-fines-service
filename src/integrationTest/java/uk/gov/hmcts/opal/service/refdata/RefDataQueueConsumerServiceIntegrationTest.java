package uk.gov.hmcts.opal.service.refdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaType;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.service.refdata.framework.RefDataQueueConsumerService;

@Transactional
@DisplayName("Ref Data Queue Consumer Integration Tests")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RefDataQueueConsumerServiceIntegrationTest extends AbstractIntegrationTest {

    private final RefDataQueueConsumerService consumer;
    private final LocalJusticeAreaRepository localJusticeAreaRepository;

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

        consumer.processMessage("""
            {
              "refDataType": "LOCAL_JUSTICE_AREA",
              "payload": {
                "ljaCode": "%s",
                "name": "Updated LJA",
                "addressLine1": "New address line 1",
                "addressLine2": "New address line 2",
                "postcode": "NE1 2BB",
                "endDate": "2027-03-04T05:06:07",
                "ljaType": "CRWCRT"
              }
            }
            """.formatted(ljaCode));

        entityManager.flush();
        entityManager.clear();

        LocalJusticeAreaEntity updated = localJusticeAreaRepository.findByLjaCode(ljaCode).orElseThrow();

        assertThat(updated.getLocalJusticeAreaId()).isEqualTo(localJusticeAreaId);
        assertThat(updated.getLjaCode()).isEqualTo(ljaCode);
        assertThat(updated.getName()).isEqualTo("Updated LJA");
        assertThat(updated.getAddressLine1()).isEqualTo("New address line 1");
        assertThat(updated.getAddressLine2()).isEqualTo("New address line 2");
        assertThat(updated.getPostcode()).isEqualTo("NE1 2BB");
        assertThat(updated.getEndDate()).isEqualTo(LocalDateTime.of(2027, 3, 4, 5, 6, 7));
        assertThat(updated.getLjaType()).isEqualTo(LocalJusticeAreaType.CRWCRT);
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

        consumer.processMessage("""
            {
              "refDataType": "LOCAL_JUSTICE_AREA",
              "payload": {
                "ljaCode": "%s",
                "name": "Created LJA",
                "addressLine1": "New address line 1",
                "addressLine2": "New address line 2",
                "postcode": "NE1 2BB",
                "endDate": "2027-03-04T05:06:07",
                "ljaType": "CRWCRT"
              }
            }
            """.formatted(ljaCode));

        entityManager.flush();
        entityManager.clear();

        LocalJusticeAreaEntity created = localJusticeAreaRepository.findByLjaCode(ljaCode).orElseThrow();

        assertThat(localJusticeAreaRepository.count()).isEqualTo(beforeCount + 1);
        assertThat(created.getLocalJusticeAreaId()).isNotNull();
        assertThat(created.getLjaCode()).isEqualTo(ljaCode);
        assertThat(created.getName()).isEqualTo("Created LJA");
        assertThat(created.getAddressLine1()).isEqualTo("New address line 1");
        assertThat(created.getAddressLine2()).isEqualTo("New address line 2");
        assertThat(created.getPostcode()).isEqualTo("NE1 2BB");
        assertThat(created.getEndDate()).isEqualTo(LocalDateTime.of(2027, 3, 4, 5, 6, 7));
        assertThat(created.getLjaType()).isEqualTo(LocalJusticeAreaType.CRWCRT);
    }

    @Test
    void processMessage_discardsInvalidPayloadWhenRequiredFieldIsMissing() {
        LocalJusticeAreaEntity original = localJusticeAreaRepository.findAll().stream()
            .findFirst()
            .orElseThrow();
        final Short localJusticeAreaId = original.getLocalJusticeAreaId();
        final String originalName = original.getName();
        final String ljaCode = original.getLjaCode() == null ? "Z125" : original.getLjaCode();
        final long beforeCount = localJusticeAreaRepository.count();

        assertThatCode(() -> consumer.processMessage("""
            {
              "refDataType": "LOCAL_JUSTICE_AREA",
              "payload": {
                "ljaCode": "%s",
                "addressLine1": "New address line 1",
                "postcode": "NE1 2BB"
              }
            }
            """.formatted(ljaCode)))
            .as("invalid ref-data messages should be discarded rather than retried")
            .doesNotThrowAnyException();

        entityManager.flush();
        entityManager.clear();

        assertThat(localJusticeAreaRepository.count()).isEqualTo(beforeCount);

        LocalJusticeAreaEntity reloaded = localJusticeAreaRepository.findById(localJusticeAreaId).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo(originalName);
        assertThat(reloaded.getLjaCode()).isEqualTo(original.getLjaCode());
    }

    @Test
    void processMessage_discardsInvalidPayloadWhenInvalidLjaTypeIsSupplied() {
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

        assertThatCode(() -> consumer.processMessage("""
            {
              "refDataType": "LOCAL_JUSTICE_AREA",
              "payload": {
                "ljaCode": "%s",
                "name": "Updated LJA",
                "addressLine1": "New address line 1",
                "addressLine2": "New address line 2",
                "postcode": "NE1 2BB",
                "ljaType": "NOT_A_REAL_TYPE"
              }
            }
            """.formatted(ljaCode)))
            .as("invalid ljaType should be discarded rather than retried")
            .doesNotThrowAnyException();

        entityManager.flush();
        entityManager.clear();

        assertThat(localJusticeAreaRepository.count()).isEqualTo(beforeCount);

        LocalJusticeAreaEntity reloaded = localJusticeAreaRepository.findById(localJusticeAreaId).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo(originalName);
        assertThat(reloaded.getLjaCode()).isEqualTo(ljaCode);
        assertThat(reloaded.getLjaType()).isEqualTo(originalType);
    }
}
