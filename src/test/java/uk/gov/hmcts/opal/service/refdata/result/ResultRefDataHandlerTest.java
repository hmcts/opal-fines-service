package uk.gov.hmcts.opal.service.refdata.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.repository.ResultRepository;

class ResultRefDataHandlerTest {
    private final JsonMapper mapper = new JsonMapper();
    private final ResultRepository repository = mock(ResultRepository.class);
    private final ResultRefDataHandler handler = new ResultRefDataHandler(repository,
        Mappers.getMapper(ResultRefDataMapper.class));

    private ResultsRecord result(String category, int priority) {
        return mapper.readValue("""
            {"result_code":"ABC","result_title":"Fine","result_type":"RESULT", "publishing_status":"Active",
             "imposition":true,"imposition_category":"%s","imposition_allocation_priority":%d,
             "imposition_creditor":"DPP","imposition_accruing":false,"enforcement":false,
             "enforcement_override":false,"further_enforcement_warning_required":false,
             "further_enforcement_disallowed":false,"enforcement_on_hold":false,"requires_enforcer":false,
             "generates_a_hearing":false,"generates_a_warrant":false,"collection_order":false,
             "disallow_ttp_extension":false,"preserve_last_enforcement_on_ttp_extension":false,
             "prevent_payment_card":false,"lists_monies":false,"allow_payment_terms":false,
             "requires_employment_data":false,"allow_additional_action":false,"requires_lja":false,
             "manual_enforcement":false,"auto_enforcement":false,"next_permitted_actions_type":"ALL",
             "next_permitted_action_count":0}
            """.formatted(category, priority), ResultsRecord.class);
    }

    @Test
    void validatesSchemaAndMapsFundWithoutUnsupportedDatabaseValue() {
        // Given
        ResultsRecord dto = result("Fund", 1);

        // When
        handler.validateDto(dto);
        ResultEntity entity = handler.createEntityFromDto(dto);

        // Then
        assertThat(entity.getResultId()).isEqualTo("ABC");
        assertThat(entity.getImpositionCategory()).isNull();
        assertThat(entity.getImpositionCreditor().getLabel()).isEqualTo("CPS");
    }

    @Test
    void rejectsSchemaInvalidAndDatabaseOverflow() {
        // Given
        ResultsRecord invalidCategory = result("invalid", 1);
        ResultsRecord invalidPriority = result("Fines", 32768);

        // When / Then
        assertThatThrownBy(() -> handler.validateDto(invalidCategory)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> handler.validateDto(invalidPriority))
            .isInstanceOf(IllegalArgumentException.class);
    }
    @Test
    void rejectsInvalidFieldValuesAndOverlongCode() {
        // Given
        ResultsRecord dto = result("Fines", 1);
        dto.setResultCode("TOOLONG");

        // When / Then
        assertThatThrownBy(() -> handler.validateDto(dto)).hasMessageContaining("result_code");

        // Given
        dto.setResultCode("ABC");
        dto.setImpositionCreditor("CPS");

        // When / Then
        assertThatThrownBy(() -> handler.validateDto(dto)).hasMessageContaining("imposition_creditor");
    }

    @Test
    void rejectsMissingRequiredValuesButAcceptsFalse() {
        // Given
        ResultsRecord dto = result("Fines", 1);

        dto.setResultCode(null);

        // When / Then
        assertThatThrownBy(() -> handler.validateDto(dto)).hasMessageContaining("result_code is required");

        // Given
        dto.setResultCode("ABC");
        dto.setResultTitle(null);

        // When / Then
        assertThatThrownBy(() -> handler.validateDto(dto)).hasMessageContaining("result_title is required");

        // Given
        dto.setResultTitle("Fine");
        dto.setResultType(null);

        // When / Then
        assertThatThrownBy(() -> handler.validateDto(dto)).hasMessageContaining("result_type is required");

        // Given
        dto.setResultType("RESULT");
        dto.setEnforcement(null);

        // When / Then
        assertThatThrownBy(() -> handler.validateDto(dto)).hasMessageContaining("enforcement is required");

        // Given
        dto.setEnforcement(false);
        dto.setPublishingStatus(null);

        // When / Then
        assertThatThrownBy(() -> handler.validateDto(dto)).hasMessageContaining("publishing_status is required");

        // Given
        dto.setPublishingStatus("Active");
        dto.setRequiresLja(null);
        dto.setManualEnforcement(null);

        // When
        handler.validateDto(dto);
    }

    @ParameterizedTest
    @MethodSource("requiredFields")
    void rejectsEachMissingRequiredField(String field, Consumer<ResultsRecord> clear) {
        // Given
        ResultsRecord dto = result("Fines", 1);
        clear.accept(dto);

        // When / Then
        assertThatThrownBy(() -> handler.validateDto(dto)).hasMessage(field + " is required");
    }

    private static Stream<Arguments> requiredFields() {
        return Stream.of(
            Arguments.of("result_code", (Consumer<ResultsRecord>) dto -> dto.setResultCode(null)),
            Arguments.of("result_title", (Consumer<ResultsRecord>) dto -> dto.setResultTitle(null)),
            Arguments.of("result_type", (Consumer<ResultsRecord>) dto -> dto.setResultType(null)),
            Arguments.of("publishing_status", (Consumer<ResultsRecord>) dto -> dto.setPublishingStatus(null)),
            Arguments.of("imposition", (Consumer<ResultsRecord>) dto -> dto.setImposition(null)),
            Arguments.of("imposition_accruing", (Consumer<ResultsRecord>) dto -> dto.setImpositionAccruing(null)),
            Arguments.of("enforcement", (Consumer<ResultsRecord>) dto -> dto.setEnforcement(null)),
            Arguments.of("enforcement_override", (Consumer<ResultsRecord>) dto -> dto.setEnforcementOverride(null)),
            Arguments.of("further_enforcement_warning_required", (Consumer<ResultsRecord>) dto -> dto.setFurtherEnforcementWarningRequired(null)),
            Arguments.of("further_enforcement_disallowed", (Consumer<ResultsRecord>) dto -> dto.setFurtherEnforcementDisallowed(null)),
            Arguments.of("enforcement_on_hold", (Consumer<ResultsRecord>) dto -> dto.setEnforcementOnHold(null)),
            Arguments.of("requires_enforcer", (Consumer<ResultsRecord>) dto -> dto.setRequiresEnforcer(null)),
            Arguments.of("generates_a_hearing", (Consumer<ResultsRecord>) dto -> dto.setGeneratesAHearing(null)),
            Arguments.of("generates_a_warrant", (Consumer<ResultsRecord>) dto -> dto.setGeneratesAWarrant(null)),
            Arguments.of("collection_order", (Consumer<ResultsRecord>) dto -> dto.setCollectionOrder(null)),
            Arguments.of("disallow_ttp_extension", (Consumer<ResultsRecord>) dto -> dto.setDisallowTtpExtension(null)),
            Arguments.of("preserve_last_enforcement_on_ttp_extension", (Consumer<ResultsRecord>) dto -> dto.setPreserveLastEnforcementOnTtpExtension(null)),
            Arguments.of("prevent_payment_card", (Consumer<ResultsRecord>) dto -> dto.setPreventPaymentCard(null)),
            Arguments.of("lists_monies", (Consumer<ResultsRecord>) dto -> dto.setListsMonies(null))
        );
    }

    @Test
    void acceptsShortBoundariesAndRejectsOverflow() {
        // Given
        ResultsRecord dto = result("Fines", 1);
        dto.setImpositionAllocationPriority(BigInteger.valueOf(Short.MIN_VALUE));

        // When
        handler.validateDto(dto);
        dto.setImpositionAllocationPriority(BigInteger.valueOf(Short.MAX_VALUE));
        handler.validateDto(dto);
        dto.setImpositionAllocationPriority(BigInteger.valueOf(Short.MIN_VALUE - 1));

        // Then
        assertThatThrownBy(() -> handler.validateDto(dto)).hasMessageContaining("database range");
    }

    @Test
    void checksMappedActionLength() {
        // Given
        ResultsRecord dto = result("Fines", 1);
        dto.setNextPermittedActionsType("SELECTED");
        dto.setResultNextPermittedActionMappings(mapper.readTree("[{\"result_code\":\"" + "A".repeat(100) + "\"}]"));

        // When
        handler.validateDto(dto);
        dto.setResultNextPermittedActionMappings(mapper.readTree("[{\"result_code\":\"" + "A".repeat(101) + "\"}]"));

        // Then
        assertThatThrownBy(() -> handler.validateDto(dto)).hasMessageContaining("database length");
    }

    @Test
    void delegatesRepositoryOperationsAndPreservesIdentityOnUpdate() {
        // Given
        ResultsRecord dto = result("Fines", 1);
        ResultEntity existing = ResultEntity.builder().resultId("ABC").resultTitle("Old").build();
        when(repository.findById("ABC")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        // When / Then
        assertThat(handler.findEntity(dto)).contains(existing);

        // When
        handler.updateEntityFromDto(dto, existing);

        // Then
        assertThat(existing.getResultId()).isEqualTo("ABC");
        assertThat(existing.getResultTitle()).isEqualTo("Fine");
        assertThat(handler.saveEntity(existing)).isSameAs(existing);

        // When
        handler.deleteEntity("ABC");

        // Then
        verify(repository).deleteById("ABC");
        assertThat(handler.payloadType()).isEqualTo(ResultsRecord.class);
        assertThat(handler.refDataType()).isEqualTo("Result");
    }

}
