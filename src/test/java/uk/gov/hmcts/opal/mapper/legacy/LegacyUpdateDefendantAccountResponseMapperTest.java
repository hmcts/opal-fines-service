package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for LegacyUpdateDefendantAccountResponseMapper default converters
 * using MapStruct-generated implementation.
 */
public class LegacyUpdateDefendantAccountResponseMapperTest {
    // Use the MapStruct-generated class directly
    private final LegacyUpdateDefendantAccountResponseMapper mapper =
        new LegacyUpdateDefendantAccountResponseMapperImpl();

    /* ------------ stringToLong() ------------ */

    @Test
    @DisplayName("stringToLong returns null for null input")
    void stringToLong_null_returnsNull() {
        assertNull(mapper.stringToLong(null));
    }

    @Test
    @DisplayName("stringToLong converts numeric string correctly")
    void stringToLong_numeric_returnsLong() {
        assertEquals(42L, mapper.stringToLong("42"));
        assertEquals(1234567890L, mapper.stringToLong("1234567890"));
    }

    @Test
    @DisplayName("stringToLong handles empty or non-numeric gracefully")
    void stringToLong_nonNumeric_returnsNullOrExceptionSafe() {
        assertNull(mapper.stringToLong(""));         // expected null
        assertNull(mapper.stringToLong("abc123"));   // invalid number -> null
    }

    /* ------------ intToLong() ------------ */

    @Test
    @DisplayName("intToLong returns null for null input")
    void intToLong_null_returnsNull() {
        assertNull(mapper.intToLong(null));
    }

    @Test
    @DisplayName("intToLong converts Integer to Long correctly")
    void intToLong_integer_returnsLong() {
        assertEquals(5L, mapper.intToLong(5));
        assertEquals(0L, mapper.intToLong(0));
        assertEquals(-7L, mapper.intToLong(-7));
    }
}
