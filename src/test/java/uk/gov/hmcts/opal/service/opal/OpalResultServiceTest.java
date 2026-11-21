package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.generated.model.GetResultByIdResponseResults;
import uk.gov.hmcts.opal.generated.model.ResultsRefDataResponse;
import uk.gov.hmcts.opal.mapper.ResultMapper;
import uk.gov.hmcts.opal.repository.ResultRepository;
import uk.gov.hmcts.opal.repository.jpa.ResultSpecs;

@ExtendWith(MockitoExtension.class)
class OpalResultServiceTest {
    @Mock
    private ResultRepository repository;
    @Mock
    private ResultSpecs specs;
    @Mock
    private SpecificationFluentQuery<ResultEntity> query;

    private OpalResultService service;

    @BeforeEach
    void setUp() {
        service = new OpalResultService(repository, Mappers.getMapper(ResultMapper.class), specs);
    }

    @ParameterizedTest
    @ValueSource(strings = {"text", "text-60", "text-100", "text-1000", "date", "integer", "decimal",
        "menu-radio", "menu-checkbox", "menu-autocomplete"})
    void getResult_addsWelshParameterForSupportedTypes(String type) {
        // Given
        String parameters = "[{\"name\":\"amount\",\"type\":\"%s\",\"language_dependent\":true}]"
            .formatted(type);
        givenResult(parameters);

        // When
        GetResultByIdResponseResults result = service.getResult("ABC", true);

        // Then
        JsonNode json = ToJsonString.toJsonNode(result.getResultParameters());
        assertEquals(2, json.size());
        assertEquals("amount", json.get(0).path("name").asText());
        assertEquals("cy_amount", json.get(1).path("name").asText());
        assertEquals(type, json.get(1).path("type").asText());
        assertEquals("Provide a welsh version for the defendant", json.get(1).path("hint").asText());
    }

    @ParameterizedTest
    @ValueSource(strings = {"[{\"name\":\"a\",\"type\":\"text\",\"language_dependent\":false}]",
        "[{\"name\":\"a\",\"type\":\"unsupported\",\"language_dependent\":true}]", "{}", "invalid"})
    void getResult_preservesUnsupportedOrInvalidParameters(String parameters) {
        // Given
        givenResult(parameters);

        // When
        GetResultByIdResponseResults result = service.getResult("ABC", true);

        // Then
        if (parameters.startsWith("[")) {
            assertEquals(1, ToJsonString.toJsonNode(result.getResultParameters()).size());
        } else {
            assertEquals(parameters, result.getResultParameters());
        }
    }

    @Test
    void getResult_withoutWelshLeavesParametersUnchanged() {
        // Given
        String parameters = "[{\"name\":\"a\",\"type\":\"text\",\"language_dependent\":true}]";
        givenResult(parameters);

        // When
        GetResultByIdResponseResults result = service.getResult("ABC", false);

        // Then
        assertEquals(parameters, result.getResultParameters());
    }

    @Test
    void getResult_throwsWhenNotFound() {
        // Given
        when(repository.findWithFullGraphByResultId("missing")).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> service.getResult("missing", false));

        // Then
        assertEquals("'Result' not found with id: missing", exception.getMessage());
    }

    @Test
    void getResultById_throwsWhenNotFound() {
        // Given
        when(repository.findById("missing")).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> service.getResultById("missing"));

        // Then
        assertEquals("'Result' not found with id: missing", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("resultIdFilters")
    @SuppressWarnings("unchecked")
    void getResultsByIds_passesFiltersAndMapsResults(List<String> ids, Optional<List<String>> expectedIds) {
        // Given
        ResultEntity entity = ResultEntity.builder().resultId("ABC").resultTitle("Result").active(true).build();
        Specification<ResultEntity> specification = (root, criteria, builder) -> builder.conjunction();
        when(specs.referenceDataByIds(expectedIds, true, false, true, false, true)).thenReturn(specification);
        when(query.sortBy(Sort.by(Sort.Direction.ASC, "resultId"))).thenReturn(query);
        Page<ResultEntity> page = new PageImpl<>(List.of(entity));
        when(query.page(Pageable.unpaged())).thenReturn(page);
        when(repository.findBy(any(Specification.class), any(Function.class))).thenAnswer(invocation ->
            ((Function<SpecificationFluentQuery<ResultEntity>, Page<ResultEntity>>) invocation.getArgument(1))
                .apply(query));

        // When
        ResultsRefDataResponse response = service.getResultsByIds(ids, true, false, true, false, true);

        // Then
        verify(specs).referenceDataByIds(expectedIds, true, false, true, false, true);
        assertEquals(1, response.getCount());
        assertEquals("ABC", response.getRefData().get(0).getResultId().get());
        assertEquals("Result", response.getRefData().get(0).getResultTitle().get());
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> resultIdFilters() {
        return Stream.of(
            org.junit.jupiter.params.provider.Arguments.of(null, Optional.empty()),
            org.junit.jupiter.params.provider.Arguments.of(List.of(), Optional.empty()),
            org.junit.jupiter.params.provider.Arguments.of(List.of("", "  "), Optional.empty()),
            org.junit.jupiter.params.provider.Arguments.of(List.of("ABC"), Optional.of(List.of("ABC")))
        );
    }

    private void givenResult(String parameters) {
        ResultEntity entity = ResultEntity.builder().resultId("ABC").resultParameters(parameters).build();
        when(repository.findWithFullGraphByResultId("ABC")).thenReturn(Optional.of(entity));
    }
}
