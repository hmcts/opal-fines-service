package uk.gov.hmcts.opal.disco.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceData;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.entity.result.ResultType;
import uk.gov.hmcts.opal.generated.model.GetResultByIdResponseResults;
import uk.gov.hmcts.opal.generated.model.ResultsRefDataResponse;
import uk.gov.hmcts.opal.generated.model.ResultsRefData;
import uk.gov.hmcts.opal.mapper.ResultMapper;
import uk.gov.hmcts.opal.repository.ResultRepository;
import uk.gov.hmcts.opal.repository.jpa.ResultSpecs;
import uk.gov.hmcts.opal.service.opal.ResultService;

@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private ResultSpecs resultSpecs;

    private Specification<ResultEntity> noOpSpec() {
        return (root, query, builder) -> builder.conjunction();
    }

    @Spy
    private ResultMapper resultMapper;

    @InjectMocks
    private ResultService resultService;

    @Test
    void testGetResult() {
        // Arrange
        ResultEntity resultEntity = ResultEntity.builder().build();
        when(resultRepository.findById(any())).thenReturn(Optional.of(resultEntity));

        // Act
        ResultEntity result = resultService.getResultById("ABC");

        // Assert
        assertNotNull(result);
        verify(resultRepository).findById("ABC");
    }

    @Test
    void testGetResultReferenceData() {
        // Arrange
        ResultEntity resultEntity = ResultEntity.builder().resultId("ABC").build();
        ResultReferenceData expectedRefData = new ResultReferenceData(
            "ABC", null, null, false, null, null, null
        );
        when(resultRepository.findById(any())).thenReturn(Optional.of(resultEntity));
        when(resultMapper.toRefData(resultEntity)).thenReturn(expectedRefData);

        // Act
        ResultReferenceData result = result_service_call_getResultRefDataById();

        // Assert
        assertNotNull(result);
        verify(resultRepository).findById("ABC");
    }

    // helper to keep arrange/act style identical to original
    private ResultReferenceData result_service_call_getResultRefDataById() {
        return resultService.getResultRefDataById("ABC");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetResultsByIds() {
        // Arrange
        ResultEntity resultEntity = ResultEntity.builder().resultId("ABC").build();
        ResultsRefData dto = ResultsRefData.builder()
            .resultId("ABC")
            .active(false)
            .build();

        @SuppressWarnings("unchecked")
        SpecificationFluentQuery<ResultEntity> sfq = (SpecificationFluentQuery<ResultEntity>)
            Mockito.mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        Page<ResultEntity> mockPage = new PageImpl<>(List.of(resultEntity), Pageable.unpaged(), 999L);

        // match the repository call with any(Specification.class), any(Function.class)
        when(resultRepository.findBy(any(Specification.class), any(Function.class))).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        when(resultMapper.toResultReferenceData(any())).thenReturn(dto);
        when(resultSpecs.referenceDataByIds(any(), any(), any(), any(), any(), any()))
            .thenReturn(noOpSpec());

        // Act
        ResultsRefDataResponse result = resultService.getResultsByIds(List.of("ABC"),
            false, false, false, false, null);

        ResultsRefDataResponse expectedResponse = ResultsRefDataResponse.builder()
            .refData(List.of(dto))
            .count(1)
            .build();

        // Assert
        assertEquals(expectedResponse.getCount(), result.getCount());
        assertEquals(expectedResponse.getRefData(), result.getRefData());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetResultsByIds_enforcementOverrideTrue() {
        // Arrange - enforcementOverride = true, other booleans null
        @SuppressWarnings("unchecked")
        SpecificationFluentQuery<ResultEntity> sfq = (SpecificationFluentQuery<ResultEntity>)
            Mockito.mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        ResultEntity resultEntity = ResultEntity.builder().resultId("NBWIT").build();
        Page<ResultEntity> mockPage = new PageImpl<>(List.of(resultEntity), Pageable.unpaged(), 1L);

        // repository stub executes the function argument provided by the service
        when(resultRepository.findBy(any(Specification.class), any(Function.class))).thenAnswer(invocation -> {
            invocation.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // ensure specsLite is stubbed (service will call it)
        when(resultSpecs.referenceDataByIds(any(), any(), any(), any(), any(), any()))
            .thenReturn(noOpSpec());

        ResultsRefData dto = ResultsRefData.builder().resultId("NBWIT").active(false).build();
        when(resultMapper.toResultReferenceData(any())).thenReturn(dto);

        // Act - enforcementOverride true (others null)
        ResultsRefDataResponse result = resultService.getResultsByIds(
            List.of("NBWIT"), null, null, null, null, Boolean.TRUE);

        // Assert
        ResultsRefDataResponse expected = ResultsRefDataResponse.builder()
            .refData(List.of(dto))
            .count(1)
            .build();

        assertEquals(expected.getCount(), result.getCount());
        assertEquals(expected.getRefData(), result.getRefData());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchResults() {
        // Arrange
        @SuppressWarnings("unchecked")
        SpecificationFluentQuery<ResultEntity> sfq = (SpecificationFluentQuery<ResultEntity>)
            Mockito.mock(SpecificationFluentQuery.class);

        ResultEntity resultEntity = ResultEntity.builder().build();
        Page<ResultEntity> mockPage = new PageImpl<>(List.of(resultEntity), Pageable.unpaged(), 999L);

        when(resultRepository.findBy(any(Specification.class), any(Function.class))).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        when(resultSpecs.findBySearchCriteria(any())).thenReturn(noOpSpec());

        // Act
        List<ResultEntity> result = resultService.searchResults(ResultSearchDto.builder().build());

        // Assert
        assertEquals(List.of(resultEntity), result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testResultsReferenceData() {
        // Arrange
        @SuppressWarnings("unchecked")
        SpecificationFluentQuery<ResultEntity> sfq = (SpecificationFluentQuery<ResultEntity>)
            Mockito.mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        ResultEntity entity = ResultEntity.builder().build();
        ResultReferenceData expectedRefData = new ResultReferenceData(
            null, null, null, false, null, null, null
        );
        when(resultMapper.toRefData(entity)).thenReturn(expectedRefData);

        Page<ResultEntity> mockPage = new PageImpl<>(List.of(entity), Pageable.unpaged(), 999L);
        when(resultRepository.findBy(any(Specification.class), any(Function.class))).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        when(resultSpecs.referenceDataFilter(any())).thenReturn(noOpSpec());

        // Act
        List<ResultReferenceData> result = resultService.getReferenceData(Optional.empty());

        ResultReferenceData refData = new ResultReferenceData(
            entity.getResultId(),
            entity.getResultTitle(),
            entity.getResultTitleCy(),
            entity.isActive(),
            entity.getResultType() == null ? null : entity.getResultType().getLabel(),
            entity.getImpositionCreditor() == null ? null : entity.getImpositionCreditor().getLabel(),
            entity.getImpositionAllocationPriority()
        );

        // Assert
        assertEquals(List.of(refData), result);
    }

    @Test
    void testGetResult_ReturnsFullDto() {
        // Arrange
        ResultEntity entity = ResultEntity.builder()
            .resultId("ABC")
            .resultTitle("Result Title")
            .resultTitleCy("Welsh Title")
            .resultType(ResultType.ACTION)
            .active(true)
            .requiresEmploymentData(true)
            .build();

        GetResultByIdResponseResults dto = GetResultByIdResponseResults.builder()
            .resultId("ABC")
            .resultTitle("Result Title")
            .resultTitleCy("Welsh Title")
            .resultType("Action")
            .active(true)
            .requiresEmploymentData(true)
            .build();

        when(resultRepository.findWithFullGraphByResultId("ABC")).thenReturn(Optional.of(entity));
        when(resultMapper.toDto(entity)).thenReturn(dto);

        // Act
        GetResultByIdResponseResults result = resultService.getResult("ABC", false);

        // Assert
        assertNotNull(result);
        assertEquals("ABC", result.getResultId());
        assertEquals("Result Title", result.getResultTitle());
        assertEquals(true, result.getRequiresEmploymentData());
        verify(resultRepository).findWithFullGraphByResultId("ABC");
        verify(resultMapper).toDto(entity);
    }

    @Test
    void getResult_whenIncludeWelshTrue_addsWelshTextParameterAfterOriginal() throws Exception {
        // Arrange
        String resultParameters = """
            [
              {
                "name": "sample_name",
                "type": "text",
                "hint": "some hint",
                "language_dependent": true
              },
              {
                "name": "sample_name_2",
                "type": "text",
                "hint": "some hint 2",
                "language_dependent": false
              }
            ]
            """;
        ResultEntity entity = ResultEntity.builder()
            .resultId("ABC")
            .resultParameters(resultParameters)
            .build();
        GetResultByIdResponseResults dto = GetResultByIdResponseResults.builder()
            .resultId("ABC")
            .resultParameters(resultParameters)
            .build();

        when(resultRepository.findWithFullGraphByResultId("ABC")).thenReturn(Optional.of(entity));
        when(resultMapper.toDto(entity)).thenReturn(dto);

        // Act
        GetResultByIdResponseResults result = resultService.getResult("ABC", true);

        // Assert
        JsonNode parameters = ToJsonString.toJsonNode(result.getResultParameters());
        assertEquals(3, parameters.size());
        assertEquals("sample_name", parameters.get(0).get("name").asText());
        assertEquals("cy_sample_name", parameters.get(1).get("name").asText());
        assertEquals("Provide a welsh version for the defendant", parameters.get(1).get("hint").asText());
        assertEquals(true, parameters.get(1).get("language_dependent").asBoolean());
        assertEquals("text", parameters.get(1).get("type").asText());
        assertEquals("sample_name_2", parameters.get(2).get("name").asText());
    }

    @ParameterizedTest
    @MethodSource("supportedParameterTypes")
    void getResult_whenIncludeWelshTrueAndSupportedType_addsWelshParameter(String type) {
        String resultParameters = """
            [
              {
                "name": "sample_name",
                "type": "%s",
                "hint": "some hint",
                "language_dependent": true
              }
            ]
            """.formatted(type);
        ResultEntity entity = ResultEntity.builder()
            .resultId("ABC")
            .resultParameters(resultParameters)
            .build();

        GetResultByIdResponseResults dto = GetResultByIdResponseResults.builder()
            .resultId("ABC")
            .resultParameters(resultParameters)
            .build();

        when(resultRepository.findWithFullGraphByResultId("ABC")).thenReturn(Optional.of(entity));
        when(resultMapper.toDto(entity)).thenReturn(dto);

        GetResultByIdResponseResults result = resultService.getResult("ABC", true);

        JsonNode parameters = ToJsonString.toJsonNode(result.getResultParameters());
        assertEquals(2, parameters.size());
        assertEquals("sample_name", parameters.get(0).get("name").asString());
        assertEquals("cy_sample_name", parameters.get(1).get("name").asString());
        assertEquals(type, parameters.get(1).get("type").asString());
        assertEquals("Provide a welsh version for the defendant", parameters.get(1).get("hint").asString());
        assertTrue(parameters.get(1).get("language_dependent").asBoolean());
    }

    @ParameterizedTest
    @MethodSource("supportedParameterTypes")
    void getResult_whenIncludeWelshTrueAndSupportedTypeNotLanguageDependent_doesNotAddWelshParameter(
        String type) throws Exception {
        String resultParameters = """
            [
              {
                "name": "sample_name",
                "type": "%s",
                "language_dependent": false
              }
            ]
            """.formatted(type);
        ResultEntity entity = ResultEntity.builder()
            .resultId("ABC")
            .resultParameters(resultParameters)
            .build();

        GetResultByIdResponseResults dto = GetResultByIdResponseResults.builder()
            .resultId("ABC")
            .resultParameters(resultParameters)
            .build();

        when(resultRepository.findWithFullGraphByResultId("ABC")).thenReturn(Optional.of(entity));
        when(resultMapper.toDto(entity)).thenReturn(dto);

        GetResultByIdResponseResults result = resultService.getResult("ABC", true);

        JsonNode parameters = ToJsonString.toJsonNode(result.getResultParameters());
        assertEquals(1, parameters.size());
        assertEquals("sample_name", parameters.get(0).get("name").asString());
    }

    private static Stream<String> supportedParameterTypes() {
        return Stream.of(
            "text-60",
            "text-100",
            "text-1000",
            "date",
            "integer",
            "decimal",
            "menu-radio",
            "menu-checkbox",
            "menu-autocomplete"
        );
    }

    @Test
    void getResult_whenIncludeWelshTrueAndUnsupportedType_doesNotAddWelshParameter() {
        String resultParameters = """
            [
              {
                "name": "sample_name",
                "type": "enforcers",
                "language_dependent": true
              }
            ]
            """;
        ResultEntity entity = ResultEntity.builder()
            .resultId("ABC")
            .resultParameters(resultParameters)
            .build();
        GetResultByIdResponseResults dto = GetResultByIdResponseResults.builder()
            .resultId("ABC")
            .resultParameters(resultParameters)
            .build();

        when(resultRepository.findWithFullGraphByResultId("ABC")).thenReturn(Optional.of(entity));
        when(resultMapper.toDto(entity)).thenReturn(dto);

        GetResultByIdResponseResults result = resultService.getResult("ABC", true);

        JsonNode parameters = ToJsonString.toJsonNode(result.getResultParameters());
        assertEquals(1, parameters.size());
        assertEquals("sample_name", parameters.get(0).get("name").asString());
    }

    @Test
    void getResult_whenIncludeWelshTrueAndNoLanguageDependentParameters_returnsOriginalParameters() throws Exception {
        // Arrange
        String resultParameters = """
            [
              {
                "name": "sample_name",
                "type": "text",
                "hint": "some hint",
                "language_dependent": false
              },
              {
                "name": "sample_name_2",
                "type": "text",
                "hint": "some hint 2",
                "language_dependent": false
              }
            ]
            """;
        ResultEntity entity = ResultEntity.builder()
            .resultId("ABC")
            .resultParameters(resultParameters)
            .build();
        GetResultByIdResponseResults dto = GetResultByIdResponseResults.builder()
            .resultId("ABC")
            .resultParameters(resultParameters)
            .build();

        when(resultRepository.findWithFullGraphByResultId("ABC")).thenReturn(Optional.of(entity));
        when(resultMapper.toDto(entity)).thenReturn(dto);

        // Act
        GetResultByIdResponseResults result = resultService.getResult("ABC", true);

        // Assert
        JsonNode parameters = ToJsonString.toJsonNode(result.getResultParameters());
        assertEquals(2, parameters.size());
        assertEquals("sample_name", parameters.get(0).get("name").asText());
        assertEquals("sample_name_2", parameters.get(1).get("name").asText());
    }

    @Test
    void getResult_whenIncludeWelshFalse_doesNotAddWelshTextParameter() throws Exception {
        // Arrange
        String resultParameters = """
            [
              {
                "name": "sample_name",
                "type": "text",
                "hint": "some hint",
                "language_dependent": true
              },
              {
                "name": "sample_name_2",
                "type": "text",
                "hint": "some hint 2",
                "language_dependent": false
              }
            ]
            """;
        ResultEntity entity = ResultEntity.builder()
            .resultId("ABC")
            .resultParameters(resultParameters)
            .build();
        GetResultByIdResponseResults dto = GetResultByIdResponseResults.builder()
            .resultId("ABC")
            .resultParameters(resultParameters)
            .build();

        when(resultRepository.findWithFullGraphByResultId("ABC")).thenReturn(Optional.of(entity));
        when(resultMapper.toDto(entity)).thenReturn(dto);

        // Act
        GetResultByIdResponseResults result = resultService.getResult("ABC", false);

        // Assert
        JsonNode parameters = ToJsonString.toJsonNode(result.getResultParameters());
        assertEquals(2, parameters.size());
        assertEquals("sample_name", parameters.get(0).get("name").asText());
        assertEquals("sample_name_2", parameters.get(1).get("name").asText());
    }

    @Test
    void testGetResult_ThrowsWhenNotFound() {
        // Arrange
        when(resultRepository.findWithFullGraphByResultId("MISSING")).thenReturn(Optional.empty());

        // Act + Assert
        org.junit.jupiter.api.Assertions.assertThrows(
            jakarta.persistence.EntityNotFoundException.class,
            () -> resultService.getResult("MISSING", false)
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetResultsByIds_allNullBooleans() {
        // Arrange - resultIds present, all Boolean flags null -> should behave like no boolean filters
        @SuppressWarnings("unchecked")
        SpecificationFluentQuery<ResultEntity> sfq = (SpecificationFluentQuery<ResultEntity>)
            Mockito.mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        ResultEntity resultEntity = ResultEntity.builder().resultId("ABC").build();
        Page<ResultEntity> mockPage = new PageImpl<>(List.of(resultEntity), Pageable.unpaged(), 1L);

        when(resultRepository.findBy(any(Specification.class), any(Function.class))).thenAnswer(invocation -> {
            invocation.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        when(resultSpecs.referenceDataByIds(any(), any(), any(), any(), any(), any()))
            .thenReturn(noOpSpec());

        ResultsRefData dto = ResultsRefData.builder().resultId("ABC").active(false).build();
        when(resultMapper.toResultReferenceData(any())).thenReturn(dto);

        // Act - pass Optional.of(ids) and null for all booleans
        ResultsRefDataResponse result = resultService.getResultsByIds(List.of("ABC"),
            null, null, null, null, null);

        // Assert - mapping and count
        ResultsRefDataResponse expected = ResultsRefDataResponse.builder()
            .refData(List.of(dto))
            .count(1)
            .build();

        assertEquals(expected.getCount(), result.getCount());
        assertEquals(expected.getRefData(), result.getRefData());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetResultsByIds_activeTrue() {
        // Arrange - active = true, other booleans null
        @SuppressWarnings("unchecked")
        SpecificationFluentQuery<ResultEntity> sfq = (SpecificationFluentQuery<ResultEntity>)
            Mockito.mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        ResultEntity resultEntity = ResultEntity.builder().resultId("ACT-1").active(true).build();
        Page<ResultEntity> mockPage = new PageImpl<>(List.of(resultEntity), Pageable.unpaged(), 1L);

        when(resultRepository.findBy(any(Specification.class), any(Function.class))).thenAnswer(invocation -> {
            invocation.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        when(resultSpecs.referenceDataByIds(any(), any(), any(), any(), any(), any()))
            .thenReturn(noOpSpec());

        ResultsRefData dto = ResultsRefData.builder().resultId("ACT-1").active(false).build();
        when(resultMapper.toResultReferenceData(any())).thenReturn(dto);

        // Act - active true (others null)
        ResultsRefDataResponse result = resultService.getResultsByIds(List.of("ACT-1"),
            Boolean.TRUE, null, null, null, null);

        // Assert
        ResultsRefDataResponse expected = ResultsRefDataResponse.builder()
            .refData(List.of(dto))
            .count(1)
            .build();

        assertEquals(expected.getCount(), result.getCount());
        assertEquals(expected.getRefData(), result.getRefData());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetResultsByIds_manualEnforcementFalse_explicitFilteringOmitted() {
        // Arrange
        @SuppressWarnings("unchecked")
        SpecificationFluentQuery<ResultEntity> sfq = (SpecificationFluentQuery<ResultEntity>)
            Mockito.mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        ResultEntity resultEntity = ResultEntity.builder()
            .resultId("MEF-FALSE")
            .manualEnforcement(false)
            .build();
        Page<ResultEntity> mockPage = new PageImpl<>(List.of(resultEntity), Pageable.unpaged(), 1L);

        when(resultRepository.findBy(any(Specification.class), any(Function.class))).thenAnswer(invocation -> {
            invocation.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        when(resultSpecs.referenceDataByIds(any(), any(), any(), any(), any(), any()))
            .thenReturn(noOpSpec());

        ResultsRefData dto = ResultsRefData.builder()
            .resultId("MEF-FALSE").active(false).build();
        when(resultMapper.toResultReferenceData(any())).thenReturn(dto);

        // Act - pass explicit false
        ResultsRefDataResponse result = resultService.getResultsByIds(List.of("MEF-FALSE"),
            null, Boolean.FALSE, null, null, null);

        // Assert
        ResultsRefDataResponse expected = ResultsRefDataResponse.builder()
            .refData(List.of(dto))
            .count(1)
            .build();

        assertEquals(expected.getCount(), result.getCount());
        assertEquals(expected.getRefData(), result.getRefData());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetResultsByIds_multipleBooleansMixed() {
        // Arrange - multiple booleans provided (true/false/null) to exercise combinations
        @SuppressWarnings("unchecked")
        SpecificationFluentQuery<ResultEntity> sfq = (SpecificationFluentQuery<ResultEntity>)
            Mockito.mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        ResultEntity resultEntity = ResultEntity.builder()
            .resultId("MIXED")
            .active(true)
            .manualEnforcement(true)
            .generatesHearing(false)
            .enforcement(true)
            .build();

        Page<ResultEntity> mockPage = new PageImpl<>(List.of(resultEntity), Pageable.unpaged(), 1L);

        when(resultRepository.findBy(any(Specification.class), any(Function.class))).thenAnswer(invocation -> {
            invocation.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        when(resultSpecs.referenceDataByIds(any(), any(), any(), any(), any(), any()))
            .thenReturn(noOpSpec());

        ResultsRefData dto = ResultsRefData.builder().resultId("MIXED").active(false).build();
        when(resultMapper.toResultReferenceData(any())).thenReturn(dto);

        // Act - mix of true/false/null
        ResultsRefDataResponse result = resultService.getResultsByIds(List.of("MIXED"),
            Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);

        // Assert
        ResultsRefDataResponse expected = ResultsRefDataResponse.builder()
            .refData(List.of(dto))
            .count(1)
            .build();

        assertEquals(expected.getCount(), result.getCount());
        assertEquals(expected.getRefData(), result.getRefData());
    }
}
