package uk.gov.hmcts.opal.service.refdata.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mapstruct.factory.Mappers;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.opal.entity.result.ImpositionCategory;
import uk.gov.hmcts.opal.entity.result.ImpositionCreditor;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.entity.result.ResultType;

class ResultRefDataMapperTest {
    private final ResultRefDataMapper mapper = Mappers.getMapper(ResultRefDataMapper.class);
    private final JsonMapper jsonMapper = new JsonMapper();

    @ParameterizedTest
    @CsvSource({"DPP,CPS", "!DPP,NOT_CPS", "CF,CF", "Any,ANY"})
    void mapsCreditorLabels(String source, ImpositionCreditor expected) {
        // Given

        // When / Then
        assertThat(mapper.creditor(source)).isEqualTo(expected);
    }

    @Test
    void mapsAndClearsFieldsOnExistingEntity() {
        // Given
        ResultsRecord dto = ResultsRecord.builder().resultTitle("Fund").resultTitleWelsh("Welsh")
            .resultType("ACTION").publishingStatus("Active").impositionCategory("Fines")
            .furtherEnforcementWarningRequired(true).generatesAWarrant(true).build();
        ResultEntity entity = ResultEntity.builder().resultId("ABC").resultTitleCy("Old")
            .impositionCategory(ImpositionCategory.COMPENSATION).build();

        // When
        mapper.updateEntityFromDto(dto, entity);

        // Then
        assertThat(entity.getResultId()).isEqualTo("ABC");
        assertThat(entity.getResultTitle()).isEqualTo("Fund");
        assertThat(entity.getResultTitleCy()).isEqualTo("Welsh");
        assertThat(entity.getResultType()).isEqualTo(ResultType.ACTION);
        assertThat(entity.isActive()).isTrue();
        assertThat(entity.isFurtherEnforcementWarn()).isTrue();
        assertThat(entity.isGeneratesWarrant()).isTrue();
        assertThat(entity.getImpositionCategory()).isEqualTo(ImpositionCategory.FINES);

        // Given
        dto.setResultTitleWelsh(null);
        dto.setImpositionCategory("Fund");
        dto.setPublishingStatus("Inactive");

        // When
        mapper.updateEntityFromDto(dto, entity);

        // Then
        assertThat(entity.getResultTitleCy()).isNull();
        assertThat(entity.getImpositionCategory()).isNull();
        assertThat(entity.isActive()).isFalse();
    }

    @Test
    void mapsActionsAndParameters() {
        // Given
        ResultsRecord dto = ResultsRecord.builder().nextPermittedActionsType("SELECTED")
            .resultNextPermittedActionMappings(jsonMapper.readTree("[{\"result_code\":\"ONE\"},"
                + "{\"result_code\":null},{\"result_code\":\"TWO\"}]"))
            .resultParameters(jsonMapper.readTree("[{\"name\":\"reason\"}]")).build();
        ResultEntity entity = new ResultEntity();

        // When
        mapper.updateEntityFromDto(dto, entity);

        // Then
        assertThat(entity.getEnfNextPermittedActions()).isEqualTo("ONE,TWO");
        assertThat(jsonMapper.readTree(entity.getResultParameters())).isEqualTo(dto.getResultParameters());

        // Given
        dto.setNextPermittedActionsType("ALL");
        dto.setResultParameters(null);

        // When
        mapper.updateEntityFromDto(dto, entity);

        // Then
        assertThat(entity.getEnfNextPermittedActions()).isEqualTo("All");
        assertThat(entity.getResultParameters()).isNull();
    }

    @Test
    void rejectsUnknownCreditorAndCategory() {
        // Given

        // When / Then
        assertThatThrownBy(() -> mapper.creditor("CPS")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> mapper.category("Other")).isInstanceOf(IllegalArgumentException.class);
        assertThat(mapper.creditor(null)).isNull();
        assertThat(mapper.category(null)).isNull();
    }
}
