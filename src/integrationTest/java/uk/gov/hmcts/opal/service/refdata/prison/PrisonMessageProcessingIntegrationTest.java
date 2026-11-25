package uk.gov.hmcts.opal.service.refdata.prison;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.refdata.RefDataHandlerRegistry;
import uk.gov.hmcts.opal.refdata.RefDataMessageDecoder;
import uk.gov.hmcts.opal.refdata.RefDataMessageProcessor;
import uk.gov.hmcts.opal.refdata.RefDataRefDataMessageProcessorImpl;
import uk.gov.hmcts.opal.refdata.RefDataUpdateHandler;
import uk.gov.hmcts.opal.refdata.schemavalidation.SchemaValidationService;
import uk.gov.hmcts.opal.repository.PrisonRepository;
import uk.gov.hmcts.opal.service.opal.PrisonService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@SpringBootTest(properties = "opal.ref-data.service-bus.consumer-enabled=false")
@Import(PrisonMessageProcessingIntegrationTest.TestRefDataConfiguration.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@JiraStory("PO-11022")
@JiraEpic("PO-10769")
class PrisonMessageProcessingIntegrationTest extends AbstractIntegrationTest {

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
        RefDataMessageDecoder refDataMessageDecoder(
            tools.jackson.databind.ObjectMapper objectMapper,
            SchemaValidationService schemaValidationService,
            RefDataHandlerRegistry handlerRegistry) {
            return new RefDataMessageDecoder(objectMapper, schemaValidationService, handlerRegistry);
        }

        @Bean
        RefDataMessageProcessor refDataMessageProcessor(
            CacheManager cacheManager,
            RefDataMessageDecoder decoder,
            RefDataHandlerRegistry handlerRegistry) {
            return new RefDataRefDataMessageProcessorImpl(cacheManager, decoder, handlerRegistry);
        }
    }

    private final RefDataMessageProcessor processor;
    private final PrisonRepository repository;
    private final PrisonRefDataHandler handler;
    private final PrisonService prisonService;
    private final CacheManager cacheManager;

    PrisonMessageProcessingIntegrationTest(
        RefDataMessageProcessor processor,
        PrisonRepository repository,
        PrisonRefDataHandler handler,
        PrisonService prisonService,
        CacheManager cacheManager) {
        this.processor = processor;
        this.repository = repository;
        this.handler = handler;
        this.prisonService = prisonService;
        this.cacheManager = cacheManager;
    }

    @Test
    @JiraStory("PO-11022")
    @JiraEpic("PO-10769")
    void processesValidPrisonMessage() {
        // Given
        String name = "Processed Prison";

        // When
        processor.processMessage(PrisonMessageBuilder.message(name, "PRIS", "Prison line 1", "PR1 1AA"));

        // Then
        try {
            assertThat(repository.findByName(name))
                .get()
                .satisfies(entity -> {
                    assertThat(entity.getName()).isEqualTo(name);
                    assertThat(entity.getAddressLine1()).isEqualTo("Prison line 1");
                    assertThat(entity.getPostcode()).isEqualTo("PR1 1AA");
                });
        } finally {
            repository.findByName(name).ifPresent(repository::delete);
        }
    }

    @Test
    @JiraStory("PO-11022")
    @JiraEpic("PO-10769")
    void usesFirstPrisAddressWhenItIsNotTheFirstAddress() {
        // Given
        String name = "Address Selection Prison";

        // When
        processor.processMessage(PrisonMessageBuilder.messageWithAddresses(name));

        // Then
        try {
            assertThat(repository.findByName(name))
                .get()
                .extracting(PrisonEntity::getAddressLine1)
                .isEqualTo("Prison line 1");
        } finally {
            repository.findByName(name).ifPresent(repository::delete);
        }
    }

    @Test
    @JiraStory("PO-11022")
    @JiraEpic("PO-10769")
    void updatesExistingPrison() {
        // Given
        String name = "Existing Prison";
        repository.saveAndFlush(PrisonEntity.builder().name(name).addressLine1("Old line").build());

        // When
        processor.processMessage(PrisonMessageBuilder.message(name, "PRIS", "Updated line", "PR4 4DD"));

        // Then
        try {
            assertThat(repository.findByName(name))
                .get()
                .satisfies(entity -> {
                    assertThat(entity.getAddressLine1()).isEqualTo("Updated line");
                    assertThat(entity.getPostcode()).isEqualTo("PR4 4DD");
                });
        } finally {
            repository.findByName(name).ifPresent(repository::delete);
        }
    }

    @Test
    @JiraStory("PO-11022")
    @JiraEpic("PO-10769")
    void rollsBackAllUpdatesWhenRecordProcessingFails() {
        // Given
        String validName = "First Prison";
        String invalidName = "A".repeat(201);

        // When / Then
        assertThatThrownBy(() -> processor.processMessage(
            PrisonMessageBuilder.messageWithProcessingFailure(validName, invalidName)))
            .isInstanceOf(RuntimeException.class);

        // Then
        assertThat(repository.findByName(validName)).isEmpty();
        assertThat(repository.findByName(invalidName)).isEmpty();
    }

    @Test
    @JiraStory("PO-11022")
    @JiraEpic("PO-10769")
    @Transactional
    void deletesPrisonUsingBusinessKey() {
        // Given
        String name = "Delete Prison";
        repository.saveAndFlush(PrisonEntity.builder().name(name).build());

        // When
        handler.deleteEntity(name);

        // Then
        assertThat(repository.findByName(name)).isEmpty();
    }
    @Test
    @JiraStory("PO-11022")
    @JiraEpic("PO-10769")
    void processingMessageClearsPrisonReferenceDataCache() {
        String name = "Cache Eviction Prison";
        cacheManager.getCache("prisonReferenceDataCache").clear();
        try {
            assertThat(prisonService.getPrisonsRefData().getPrisons())
                .noneMatch(prison -> name.equals(prison.getName()));
            repository.saveAndFlush(PrisonEntity.builder().name(name).build());
            assertThat(prisonService.getPrisonsRefData().getPrisons())
                .noneMatch(prison -> name.equals(prison.getName()));

            processor.processMessage(PrisonMessageBuilder.message(name, "PRIS", "Updated line", "PR4 4DD"));

            assertThat(repository.findByName(name)).get()
                .extracting(PrisonEntity::getAddressLine1).isEqualTo("Updated line");
            assertThat(prisonService.getPrisonsRefData().getPrisons())
                .anyMatch(prison -> name.equals(prison.getName()));
        } finally {
            repository.findByName(name).ifPresent(repository::delete);
            cacheManager.getCache("prisonReferenceDataCache").clear();
        }
    }
}
