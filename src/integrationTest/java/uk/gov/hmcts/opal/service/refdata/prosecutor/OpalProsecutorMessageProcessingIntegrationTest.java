package uk.gov.hmcts.opal.service.refdata.prosecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.OpalProsecutorEntity;
import uk.gov.hmcts.opal.refdata.RefDataHandlerRegistry;
import uk.gov.hmcts.opal.refdata.RefDataMessageDecoder;
import uk.gov.hmcts.opal.refdata.RefDataMessageProcessor;
import uk.gov.hmcts.opal.refdata.RefDataRefDataMessageProcessorImpl;
import uk.gov.hmcts.opal.refdata.RefDataUpdateHandler;
import uk.gov.hmcts.opal.refdata.schemavalidation.SchemaValidationService;
import uk.gov.hmcts.opal.repository.OpalProsecutorRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@SpringBootTest(properties = "opal.ref-data.service-bus.consumer-enabled=false")
@Import(OpalProsecutorMessageProcessingIntegrationTest.TestRefDataConfiguration.class)
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class OpalProsecutorMessageProcessingIntegrationTest extends AbstractIntegrationTest {

    private static final String VALID_CODE = "9971";
    private static final String UPDATE_CODE = "9972";
    private static final String ROLLBACK_VALID_CODE = "9973";
    private static final String ROLLBACK_INVALID_CODE = "9974";

    private final RefDataMessageProcessor processor;
    private final OpalProsecutorRepository repository;

    @TestConfiguration
    static class TestRefDataConfiguration {

        @Bean
        RefDataHandlerRegistry refDataHandlerRegistry(List<RefDataUpdateHandler<?, ?>> handlers) {
            return new RefDataHandlerRegistry(handlers);
        }

        @Bean
        SchemaValidationService schemaValidationService() {
            return new SchemaValidationService();
        }

        @Bean
        RefDataMessageDecoder refDataMessageDecoder(ObjectMapper objectMapper,
            SchemaValidationService schemaValidationService, RefDataHandlerRegistry handlerRegistry) {
            return new RefDataMessageDecoder(objectMapper, schemaValidationService, handlerRegistry);
        }

        @Bean
        RefDataMessageProcessor refDataMessageProcessor(CacheManager cacheManager, RefDataMessageDecoder decoder,
            RefDataHandlerRegistry handlerRegistry) {
            return new RefDataRefDataMessageProcessorImpl(cacheManager, decoder, handlerRegistry);
        }
    }

    @Test
    @JiraStory("PO-11021")
    @JiraEpic("PO-9031")
    @JiraTestKey("PO-11021")
    void processesValidProsecutorMessageUsingProsecutorAddress() {
        try {
            processor.processMessage(OpalProsecutorMessageBuilder.messageWithAddresses(Integer.parseInt(VALID_CODE)));

            assertThat(repository.findById(VALID_CODE))
                .get()
                .satisfies(entity -> {
                    assertThat(entity.getProsecutorCode()).isEqualTo(VALID_CODE);
                    assertThat(entity.getName()).isEqualTo("Test Prosecutor");
                    assertThat(entity.getAddressLine1()).isEqualTo("Prosecutor line 1");
                    assertThat(entity.getAddressLine2()).isEqualTo("Prosecutor line 2");
                    assertThat(entity.getAddressLine3()).isEqualTo("Prosecutor line 3");
                    assertThat(entity.getAddressLine4()).isEqualTo("Prosecutor line 4");
                    assertThat(entity.getAddressLine5()).isEqualTo("Prosecutor line 5");
                    assertThat(entity.getPostcode()).isEqualTo("PR1 1AA");
                    assertThat(entity.getEndDate()).isNull();
                });
        } finally {
            repository.deleteById(VALID_CODE);
        }
    }

    @Test
    @JiraStory("PO-11021")
    @JiraEpic("PO-9031")
    @JiraTestKey("PO-11021")
    void updatesExistingProsecutorWithoutUpdatingEndDateAndClearsNullableAddress() {
        LocalDateTime existingEndDate = LocalDateTime.of(2027, 3, 4, 0, 0);
        OpalProsecutorEntity existing = OpalProsecutorEntity.builder()
            .prosecutorCode(UPDATE_CODE)
            .name("Existing Prosecutor")
            .addressLine1("Existing address")
            .postcode("PR1 1AA")
            .endDate(existingEndDate)
            .build();
        repository.saveAndFlush(existing);

        try {
            processor.processMessage(OpalProsecutorMessageBuilder.messageWithEndDateAndNoAddresses(
                Integer.parseInt(UPDATE_CODE), "2028-03-04"));

            assertThat(repository.findById(UPDATE_CODE))
                .get()
                .satisfies(entity -> {
                    assertThat(entity.getName()).isEqualTo("Updated Prosecutor");
                    assertThat(entity.getAddressLine1()).isNull();
                    assertThat(entity.getPostcode()).isNull();
                    assertThat(entity.getEndDate()).isEqualTo(existingEndDate);
                });
        } finally {
            repository.deleteById(UPDATE_CODE);
        }
    }

    @Test
    @JiraStory("PO-11021")
    @JiraEpic("PO-9031")
    @JiraTestKey("PO-11021")
    void rejectsProsecutorCodeThatExceedsDatabaseLength() {
        String invalidCode = "99715";

        assertThatThrownBy(() -> processor.processMessage(
            OpalProsecutorMessageBuilder.validMessage(Integer.parseInt(invalidCode))))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("prosecutor_code must not exceed 4 characters");

        assertThat(repository.findById(invalidCode)).isEmpty();
    }

    @Test
    @JiraStory("PO-11021")
    @JiraEpic("PO-9031")
    @JiraTestKey("PO-11021")
    void rollsBackAllUpdatesWhenOneProsecutorRecordFails() {
        try {
            assertThatThrownBy(() -> processor.processMessage(
                OpalProsecutorMessageBuilder.messageWithProcessingFailure(
                    Integer.parseInt(ROLLBACK_VALID_CODE), Integer.parseInt(ROLLBACK_INVALID_CODE))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("addresses must contain exactly one Prosecutor address");

            assertThat(repository.findById(ROLLBACK_VALID_CODE)).isEmpty();
            assertThat(repository.findById(ROLLBACK_INVALID_CODE)).isEmpty();
        } finally {
            repository.deleteById(ROLLBACK_VALID_CODE);
            repository.deleteById(ROLLBACK_INVALID_CODE);
        }
    }
}
