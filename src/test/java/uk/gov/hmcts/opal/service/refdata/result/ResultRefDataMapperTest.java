package uk.gov.hmcts.opal.service.refdata.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mapstruct.factory.Mappers;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.opal.entity.result.ImpositionCategory;
import uk.gov.hmcts.opal.entity.result.ImpositionCreditor;
import uk.gov.hmcts.opal.entity.result.ResultOpalEntity;
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
        ResultOpalEntity entity = ResultOpalEntity.builder().resultId("ABC").resultTitleCy("Old")
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
    void mapsRenamedBooleanFieldsOnUpdate() {
        // Given
        ResultsRecord dto = ResultsRecord.builder()
            .furtherEnforcementDisallowed(true)
            .enforcementOnHold(true)
            .generatesAHearing(true)
            .disallowTtpExtension(true)
            .preserveLastEnforcementOnTtpExtension(true)
            .build();
        ResultOpalEntity entity = new ResultOpalEntity();

        // When
        mapper.updateEntityFromDto(dto, entity);

        // Then
        assertThat(entity.isFurtherEnforcementDisallow()).isTrue();
        assertThat(entity.isEnforcementHold()).isTrue();
        assertThat(entity.isGeneratesHearing()).isTrue();
        assertThat(entity.isExtendTtpDisallow()).isTrue();
        assertThat(entity.isExtendTtpPreserveLastEnf()).isTrue();

        // Given
        dto.setFurtherEnforcementDisallowed(false);
        dto.setEnforcementOnHold(false);
        dto.setGeneratesAHearing(false);
        dto.setDisallowTtpExtension(false);
        dto.setPreserveLastEnforcementOnTtpExtension(false);

        // When
        mapper.updateEntityFromDto(dto, entity);

        // Then
        assertThat(entity.isFurtherEnforcementDisallow()).isFalse();
        assertThat(entity.isEnforcementHold()).isFalse();
        assertThat(entity.isGeneratesHearing()).isFalse();
        assertThat(entity.isExtendTtpDisallow()).isFalse();
        assertThat(entity.isExtendTtpPreserveLastEnf()).isFalse();
    }

    @Test
    void mapsActionsAndParameters() {
        // Given
        ResultsRecord dto = ResultsRecord.builder().nextPermittedActionsType("SELECTED")
            .resultNextPermittedActionMappings(jsonMapper.readTree("""
                [
                  {"result_code": "ONE"},
                  {"result_code": null},
                  {"result_code": "TWO"}
                ]
                """))
            .resultParameters(jsonMapper.readTree("""
                [{"name": "reason"}]
                """)).build();
        ResultOpalEntity entity = new ResultOpalEntity();

        // When
        mapper.updateEntityFromDto(dto, entity);

        // Then
        assertThat(entity.getEnfNextPermittedActions()).isEqualTo("ONE,TWO");
        assertThat(jsonMapper.readTree(entity.getResultParameters())).isEqualTo(jsonMapper.readTree("""
            [{
              "name": "reason",
              "hint": "",
              "options": [],
              "apidata": ""
            }]
            """));

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
    void renamesLanguageDependentAndValidatesParameters() {
        JsonNode input = jsonMapper.readTree("""
            [
              {"language_dependent": null},
              {"languageDependent": true}
            ]
            """);
        assertThat(jsonMapper.readTree(mapper.parameters(input)))
            .isEqualTo(jsonMapper.readTree("""
                [
                  {
                    "languageDependent": null,
                    "hint": "",
                    "options": [],
                    "apidata": ""
                  },
                  {
                    "languageDependent": true,
                    "hint": "",
                    "options": [],
                    "apidata": ""
                  }
                ]
                """));
        assertThat(input.get(0).has("language_dependent")).isTrue();
        assertThat(mapper.parameters(jsonMapper.readTree("null"))).isNull();
        assertThatThrownBy(() -> mapper.parameters(jsonMapper.readTree("{}")))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> mapper.parameters(jsonMapper.readTree("[null]")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void defaultsMissingAndNullParameterAttributesAndPreservesProvidedValues() {
        JsonNode input = jsonMapper.readTree("""
            [
              {"name": "missing"},
              {
                "hint": null,
                "options": null,
                "apidata": null
              },
              {
                "hint": "Choose",
                "options": ["one"],
                "apidata": "courts"
              }
            ]
            """);

        assertThat(jsonMapper.readTree(mapper.parameters(input))).isEqualTo(jsonMapper.readTree("""
            [
              {
                "name": "missing",
                "hint": "",
                "options": [],
                "apidata": ""
              },
              {
                "hint": "",
                "options": [],
                "apidata": ""
              },
              {
                "hint": "Choose",
                "options": ["one"],
                "apidata": "courts"
              }
            ]
            """));
        assertThat(input.get(0).has("hint")).isFalse();
        assertThat(input.get(1).get("hint").isNull()).isTrue();
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
