package uk.gov.hmcts.opal.mapper.helper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createComplexReportParameters;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.opal.service.report.ReportParameterData;

class JsonMapperHelperTest {

    private JsonMapperHelper jsonMapperHelper;

    @BeforeEach
    void setUp() {
        jsonMapperHelper = new JsonMapperHelper(new ObjectMapper());
    }

    @Nested
    class ParseJsonToMapSuccessCases {

        @Test
        void parseJsonToMap_withValidJson_shouldReturnMap() {
            String json = """
                {
                    "key1": "value1",
                    "key2": "value2",
                    "key3": 123
                }""";

            Map<String, Object> actual = jsonMapperHelper.parseJsonToMap(json);

            assertAll("Verify valid JSON parsing",
                () -> assertNotNull(actual),
                () -> assertEquals(3, actual.size()),
                () -> assertEquals("value1", actual.get("key1")),
                () -> assertEquals("value2", actual.get("key2")),
                () -> assertEquals(123, actual.get("key3"))
            );
        }

        @Test
        void parseJsonToMap_withNestedJson_shouldReturnMap() {
            String json = """
                {
                    "outer": {"inner": "value"},
                    "top": "level"
                }""";

            Map<String, Object> actual = jsonMapperHelper.parseJsonToMap(json);

            assertAll("Verify nested JSON parsing",
                () -> assertNotNull(actual),
                () -> assertEquals(2, actual.size()),
                () -> assertInstanceOf(Map.class, actual.get("outer")),
                () -> assertEquals("level", actual.get("top"))
            );
        }

        @Test
        void parseJsonToMap_withArrayValues_shouldReturnMap() {
            String json = """
                {
                    "items": ["item1", "item2"],
                    "count": 2
                }""";

            Map<String, Object> actual = jsonMapperHelper.parseJsonToMap(json);

            assertAll("Verify JSON with arrays parsing",
                () -> assertNotNull(actual),
                () -> assertEquals(2, actual.size()),
                () -> assertInstanceOf(List.class, actual.get("items")),
                () -> assertEquals(2, actual.get("count"))
            );
        }
    }

    @Nested
    class ParseJsonToMapEmptyNullCases {

        @ParameterizedTest(name = "Should return empty map when JSON is {0}")
        @NullSource
        @ValueSource(strings = {"", "{}"})
        void parseJsonToMap_withNullOrEmptyJson_shouldReturnEmptyMap(String json) {
            Map<String, Object> actual = jsonMapperHelper.parseJsonToMap(json);

            assertAll(
                () -> assertNotNull(actual),
                () -> assertTrue(actual.isEmpty())
            );
        }
    }

    @Nested
    class ParseJsonToMapErrorCases {

        @ParameterizedTest(name = "Should throw IllegalArgumentException for invalid JSON: {0}")
        @ValueSource(strings = {
            "{invalid json}",
            "{\"key\":\"value\"",
            "{\"key\":{\"nested\":\"value\""
        })
        void parseJsonToMap_withInvalidJson_shouldThrowIllegalArgumentException(String invalidJson) {
            assertThrows(IllegalArgumentException.class,
                () -> jsonMapperHelper.parseJsonToMap(invalidJson),
                "Expected IllegalArgumentException for invalid/malformed JSON input"
            );
        }

        @SuppressWarnings("unchecked")
        @Test
        void parseJsonToMap_whenObjectMapperThrowsJacksonException_shouldThrowIllegalArgumentException()
            throws JacksonException {

            ObjectMapper objectMapper = org.mockito.Mockito.mock(ObjectMapper.class);
            JsonMapperHelper helper = new JsonMapperHelper(objectMapper);
            String json = "{\"key\":\"value\"}";
            JacksonException parseException = new JacksonException("bad json") {
            };
            when(objectMapper.readValue(eq(json), any(TypeReference.class))).thenThrow(parseException);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> helper.parseJsonToMap(json));

            assertEquals("Invalid JSON in report_parameters: " + json, exception.getMessage());
            assertEquals(parseException, exception.getCause());
        }
    }

    @Nested
    class ReportParametersToMapSuccessCases {
        @Test
        @DisplayName("Should parse complex JSON parameters correctly")
        void reportParametersToMap_fullParameters_shouldParseCorrectly() {
            List<ReportParameterData> reportParameters = createComplexReportParameters();

            Map<?, ?> params = jsonMapperHelper.reportParametersToMap(reportParameters);

            assertAll(
                () -> assertEquals(9, params.size()),
                () -> assertEquals("date", ((Map<?, ?>)params.get("date-param")).get("type")),
                () -> assertEquals(true, ((Map<?, ?>)params.get("date-param")).get("mandatory")),
                () -> assertEquals("decimal-2dp", ((Map<?, ?>)params.get("decimal-param")).get("type")),
                () -> assertEquals(1.0, ((Map<?, ?>)params.get("decimal-param")).get("min")),
                () -> assertEquals(10.0, ((Map<?, ?>)params.get("decimal-param")).get("max")),
                () -> assertEquals("integer", ((Map<?, ?>)params.get("integer-param")).get("type")),
                () -> assertEquals(1L, ((Map<?, ?>)params.get("integer-param")).get("min")),
                () -> assertEquals(10L, ((Map<?, ?>)params.get("integer-param")).get("max")),
                () -> assertEquals("menu-radio", ((Map<?, ?>)params.get("radio-param")).get("type")),
                () -> assertEquals(1, ((Map<?, ?>)params.get("radio-param")).get("min")),
                () -> assertEquals(1, ((Map<?, ?>)params.get("radio-param")).get("max")),
                () -> assertEquals(List.of("one", "two"), ((Map<?, ?>)params.get("radio-param")).get("options")),
                () -> assertEquals("menu-checkbox", ((Map<?, ?>)params.get("checkbox-param")).get("type")),
                () -> assertEquals(1, ((Map<?, ?>)params.get("checkbox-param")).get("min")),
                () -> assertEquals(2, ((Map<?, ?>)params.get("checkbox-param")).get("max")),
                () -> assertEquals(List.of("one", "two"), ((Map<?, ?>)params.get("checkbox-param")).get("options")),
                () -> assertEquals("menu-autocomplete", ((Map<?, ?>)params.get("autocomplete-param")).get("type")),
                () -> assertEquals("text-60", ((Map<?, ?>)params.get("text-60-param")).get("type")),
                () -> assertEquals(1, ((Map<?, ?>)params.get("text-60-param")).get("min")),
                () -> assertEquals(60, ((Map<?, ?>)params.get("text-60-param")).get("max")),
                () -> assertEquals("text-100", ((Map<?, ?>)params.get("text-100-param")).get("type")),
                () -> assertEquals(1, ((Map<?, ?>)params.get("text-100-param")).get("min")),
                () -> assertEquals(100, ((Map<?, ?>)params.get("text-100-param")).get("max")),
                () -> assertEquals("text-1000", ((Map<?, ?>)params.get("text-1000-param")).get("type")),
                () -> assertEquals(1, ((Map<?, ?>)params.get("text-1000-param")).get("min")),
                () -> assertEquals(1000, ((Map<?, ?>)params.get("text-1000-param")).get("max"))
            );
        }

        @Test
        @DisplayName("Should parse empty JSON parameters correctly")
        void reportParametersToMap_emptyParameters_shouldParseCorrectly() {
            List<ReportParameterData> reportParameters = List.of();

            Map<?, ?> params = jsonMapperHelper.reportParametersToMap(reportParameters);
            assertEquals(0, params.size());
        }

        @Test
        @DisplayName("Should parse null JSON parameters correctly")
        void reportParametersToMap_nullParameters_shouldParseCorrectly() {
            List<ReportParameterData> reportParameters = null;

            Map<?, ?> params = jsonMapperHelper.reportParametersToMap(reportParameters);
            assertEquals(0, params.size());
        }
    }
}
