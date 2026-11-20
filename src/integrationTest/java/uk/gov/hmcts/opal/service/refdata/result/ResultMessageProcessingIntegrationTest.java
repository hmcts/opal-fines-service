package uk.gov.hmcts.opal.service.refdata.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.result.ImpositionCategory;
import uk.gov.hmcts.opal.entity.result.ImpositionCreditor;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.entity.result.ResultType;
import uk.gov.hmcts.opal.refdata.RefDataHandlerRegistry;
import uk.gov.hmcts.opal.refdata.RefDataMessageDecoder;
import uk.gov.hmcts.opal.refdata.RefDataMessageProcessor;
import uk.gov.hmcts.opal.refdata.RefDataRefDataMessageProcessorImpl;
import uk.gov.hmcts.opal.refdata.RefDataUpdateHandler;
import uk.gov.hmcts.opal.refdata.schemavalidation.SchemaValidationService;
import uk.gov.hmcts.opal.repository.OpalResultRepository;

@SpringBootTest(properties = "opal.ref-data.service-bus.consumer-enabled=true")
@ActiveProfiles("integration")
@Isolated
class ResultMessageProcessingIntegrationTest extends AbstractIntegrationTest {
    private static final String RESULT_ID = "ZRIT01";
    private static final String SECOND_ID = "ZRIT02";

    @Autowired
    private RefDataMessageProcessor processor;
    @Autowired
    private OpalResultRepository repository;
    @Autowired
    private ResultRefDataHandler handler;

    @AfterEach
    void cleanup() {
        repository.deleteAllById(List.of(RESULT_ID, SECOND_ID));
        repository.flush();
    }

    @Test
    void insertsResultFromValidMessage() {
        // Given
        ObjectNode result = result(RESULT_ID, "New result", "Fines", "DPP");

        // When
        processor.processMessage(message(result));

        // Then
        assertThat(repository.findById(RESULT_ID)).get().satisfies(entity -> {
            assertThat(entity.getResultTitle()).isEqualTo("New result");
            assertThat(entity.getResultType()).isEqualTo(ResultType.RESULT);
            assertThat(entity.getImpositionCategory()).isEqualTo(ImpositionCategory.FINES);
            assertThat(entity.getImpositionCreditor()).isEqualTo(ImpositionCreditor.CPS);
            assertThat(entity.getImpositionAllocationPriority()).isEqualTo((short) 2);
            assertThat(entity.isActive()).isTrue();
            assertThat(entity.getEnfNextPermittedActions()).isEqualTo("All");
        });
    }

    @Test
    void updatesExistingResultAndClearsNullableFields() {
        // Given
        repository.saveAndFlush(ResultEntity.builder().resultId(RESULT_ID).resultTitle("Old title")
            .resultTitleCy("Old Welsh").resultType(ResultType.ACTION).impositionCategory(ImpositionCategory.COSTS)
            .build());
        ObjectNode result = result(RESULT_ID, "Updated title", "Compensation", "Any");
        result.put("publishing_status", "Inactive");

        // When
        processor.processMessage(message(result));

        // Then
        assertThat(repository.findById(RESULT_ID)).get().satisfies(entity -> {
            assertThat(entity.getResultTitle()).isEqualTo("Updated title");
            assertThat(entity.getResultTitleCy()).isNull();
            assertThat(entity.getResultType()).isEqualTo(ResultType.RESULT);
            assertThat(entity.getImpositionCategory()).isEqualTo(ImpositionCategory.COMPENSATION);
            assertThat(entity.getImpositionCreditor()).isEqualTo(ImpositionCreditor.ANY);
            assertThat(entity.isActive()).isFalse();
        });
    }

    @ParameterizedTest
    @CsvSource({"DPP,CPS", "!DPP,NOT_CPS", "CF,CF", "Any,ANY"})
    void persistsEverySchemaCreditor(String source, ImpositionCreditor expected) {
        // Given
        ObjectNode result = result(RESULT_ID, "Creditor", "Fines", source);

        // When
        processor.processMessage(message(result));

        // Then
        assertThat(repository.findById(RESULT_ID)).get().extracting(ResultEntity::getImpositionCreditor)
            .isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"Compensation,COMPENSATION", "Costs,COSTS", "Court Charge,COURT_CHARGE",
        "Crown Prosecution Costs,CROWN_PROSECUTION_COSTS", "Fines,FINES", "Legal Aid,LEGAL_AID",
        "Victim Surcharge,VICTIM_SURCHARGE", "Witness Expenses & Central Fund,WITNESS_EXPENSES_AND_CENTRAL_FUND"})
    void persistsSupportedSchemaCategories(String category, ImpositionCategory expected) {
        // Given
        ObjectNode result = result(RESULT_ID, "Category", category, "CF");

        // When
        processor.processMessage(message(result));

        // Then
        assertThat(repository.findById(RESULT_ID)).get().extracting(ResultEntity::getImpositionCategory)
            .isEqualTo(expected);
    }

    @Test
    void storesNullForFundUntilDatabaseSupportsIt() {
        // Given
        ObjectNode result = result(RESULT_ID, "Fund", "Fund", "CF");

        // When
        processor.processMessage(message(result));

        // Then
        assertThat(repository.findById(RESULT_ID)).get().extracting(ResultEntity::getImpositionCategory).isNull();
    }

    @Test
    void rejectsSchemaInvalidMessageWithoutWriting() {
        // Given
        ObjectNode result = result(RESULT_ID, "Invalid", "Fines", "CF");
        result.put("result_type", "OTHER");

        // When / Then
        assertThatThrownBy(() -> processor.processMessage(message(result))).isInstanceOf(RuntimeException.class);
        assertThat(repository.findById(RESULT_ID)).isEmpty();
    }

    @Test
    void rollsBackEntireMessageWhenSecondRecordExceedsDatabaseRange() {
        // Given
        ObjectNode first = result(RESULT_ID, "First", "Fines", "CF");
        ObjectNode second = result(SECOND_ID, "Second", "Fines", "CF");
        second.put("imposition_allocation_priority", 32768);

        // When / Then
        assertThatThrownBy(() -> processor.processMessage(message(first, second)))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("database range");
        assertThat(repository.findById(RESULT_ID)).isEmpty();
        assertThat(repository.findById(SECOND_ID)).isEmpty();
    }

    @Test
    void deletesResultByBusinessKey() {
        // Given
        processor.processMessage(message(result(RESULT_ID, "Delete", "Fines", "CF")));

        // When
        handler.deleteEntity(RESULT_ID);

        // Then
        assertThat(repository.findById(RESULT_ID)).isEmpty();
    }

    private ObjectNode result(String code, String title, String category, String creditor) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("result_code", code);
        result.put("result_title", title);
        result.put("result_type", "RESULT");
        result.put("publishing_status", "Active");
        result.put("imposition", true);
        result.put("imposition_category", category);
        result.put("imposition_allocation_priority", 2);
        result.put("imposition_creditor", creditor);
        result.put("imposition_accruing", false);
        for (String field : List.of("enforcement", "enforcement_override", "further_enforcement_warning_required",
            "further_enforcement_disallowed", "enforcement_on_hold", "requires_enforcer", "generates_a_hearing",
            "generates_a_warrant", "collection_order", "disallow_ttp_extension",
            "preserve_last_enforcement_on_ttp_extension", "prevent_payment_card", "lists_monies",
            "allow_payment_terms", "requires_employment_data", "allow_additional_action", "requires_lja",
            "manual_enforcement", "auto_enforcement")) {
            result.put(field, false);
        }
        result.put("next_permitted_actions_type", "ALL");
        result.put("next_permitted_action_count", 0);
        return result;
    }

    private String message(ObjectNode... records) {
        ObjectNode message = objectMapper.createObjectNode();
        ObjectNode header = message.putObject("header");
        header.put("message_id", UUID.randomUUID().toString());
        header.put("message_type", "ReferenceData");
        header.put("data_product", "Result");
        header.put("operation", "PUBLISH");
        header.put("source_system", "Semarchy");
        header.put("created_date_time", "2026-01-01T00:00:00Z");
        header.put("release_package_id", 1);
        header.put("record_count", records.length);
        ArrayNode array = message.putObject("payload").putArray("records");
        for (ObjectNode result : records) {
            array.add(result);
        }
        return objectMapper.writeValueAsString(message);
    }
}
