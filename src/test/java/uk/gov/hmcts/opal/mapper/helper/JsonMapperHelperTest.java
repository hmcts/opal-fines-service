package uk.gov.hmcts.opal.mapper.helper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for JsonMapperHelper.
 *
 * @author Krishna Sapkota
 */
@DisplayName("JsonMapperHelper Tests [@PO-2250]")
class JsonMapperHelperTest {

    private JsonMapperHelper cut;

    @BeforeEach
    void setUp() {
        cut = new JsonMapperHelper(new ObjectMapper());
    }

    @Nested
    @DisplayName("parseJsonToMap() - Success Cases")
    class ParseJsonToMapSuccessCases {

        @Test
        @DisplayName("Should parse valid JSON with multiple fields")
        void parseJsonToMap_withValidJson_shouldReturnMap() {
            String json = "{\"key1\":\"value1\",\"key2\":\"value2\",\"key3\":123}";

            Map<String, Object> actual = cut.parseJsonToMap(json);

            assertAll("Verify valid JSON parsing",
                () -> assertNotNull(actual),
                () -> assertEquals(3, actual.size()),
                () -> assertEquals("value1", actual.get("key1")),
                () -> assertEquals("value2", actual.get("key2")),
                () -> assertEquals(123, actual.get("key3"))
            );
        }

        @Test
        @DisplayName("Should parse JSON with nested objects")
        void parseJsonToMap_withNestedJson_shouldReturnMap() {
            String json = "{\"outer\":{\"inner\":\"value\"},\"top\":\"level\"}";

            Map<String, Object> actual = cut.parseJsonToMap(json);

            assertAll("Verify nested JSON parsing",
                () -> assertNotNull(actual),
                () -> assertEquals(2, actual.size()),
                () -> assertInstanceOf(Map.class, actual.get("outer")),
                () -> assertEquals("level", actual.get("top"))
            );
        }

        @Test
        @DisplayName("Should parse JSON with array values")
        void parseJsonToMap_withArrayValues_shouldReturnMap() {
            String json = "{\"items\":[\"item1\",\"item2\"],\"count\":2}";

            Map<String, Object> actual = cut.parseJsonToMap(json);

            assertAll("Verify JSON with arrays parsing",
                () -> assertNotNull(actual),
                () -> assertEquals(2, actual.size()),
                () -> assertInstanceOf(List.class, actual.get("items")),
                () -> assertEquals(2, actual.get("count"))
            );
        }
    }

    @Nested
    @DisplayName("parseJsonToMap() - Empty/Null Cases")
    class ParseJsonToMapEmptyNullCases {

        @ParameterizedTest(name = "Should return null when JSON is {0}")
        @NullSource
        @ValueSource(strings = {"", "{}"})
        @DisplayName("Should return null for null, empty string, or empty object")
        void parseJsonToMap_withNullOrEmptyJson_shouldReturnNull(String json) {
            Map<String, Object> actual = cut.parseJsonToMap(json);

            assertNull(actual, "Expected null for null/empty/empty-object JSON input");
        }
    }

    @Nested
    @DisplayName("parseJsonToMap() - Error Cases")
    class ParseJsonToMapErrorCases {

        @ParameterizedTest(name = "Should return null for invalid JSON: {0}")
        @ValueSource(strings = {
            "{invalid json}",
            "{\"key\":\"value\"",
            "{\"key\":{\"nested\":\"value\""
        })
        @DisplayName("Should return null when JSON is invalid or malformed")
        void parseJsonToMap_withInvalidJson_shouldReturnNull(String invalidJson) {
            Map<String, Object> actual = cut.parseJsonToMap(invalidJson);

            assertNull(actual, "Expected null for invalid/malformed JSON input");
        }
    }
}