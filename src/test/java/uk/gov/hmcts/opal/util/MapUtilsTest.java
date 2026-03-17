package uk.gov.hmcts.opal.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapUtilsTest {

    @Test
    void givenNullValue_whenOfNullable_thenReturnsMapContainingNullValue() {
        Map<String, Object> data = MapUtils.ofNullable(
            "firstKey", 123L,
            "secondKey", null,
            "thirdKey", "value"
        );

        assertEquals(3, data.size());
        assertEquals(123L, data.get("firstKey"));
        assertNull(data.get("secondKey"));
        assertEquals("value", data.get("thirdKey"));
        assertInstanceOf(java.util.LinkedHashMap.class, data);
    }

    @Test
    void givenOddNumberOfArguments_whenOfNullable_thenThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> MapUtils.ofNullable("firstKey", 123L, "secondKey"));

        assertEquals("Key/value arguments must be supplied in pairs.", exception.getMessage());
    }

    @Test
    void givenNullKey_whenOfNullable_thenThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> MapUtils.ofNullable(null, "value"));

        assertEquals("Map keys must not be null.", exception.getMessage());
    }
}
