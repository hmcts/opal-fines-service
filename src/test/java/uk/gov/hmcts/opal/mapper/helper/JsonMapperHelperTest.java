package uk.gov.hmcts.opal.mapper.helper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

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
    }
}