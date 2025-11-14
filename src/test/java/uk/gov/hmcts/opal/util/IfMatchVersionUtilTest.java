package uk.gov.hmcts.opal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class IfMatchVersionUtilTest {

    @Test
    @DisplayName("null/blank -> 1")
    void nullOrBlank() {
        assertEquals(1, IfMatchVersionUtil.parseIfMatchVersion(null));
        assertEquals(1, IfMatchVersionUtil.parseIfMatchVersion(""));
        assertEquals(1, IfMatchVersionUtil.parseIfMatchVersion("   "));
    }

    @Test @DisplayName("Quoted / weak validators parse their digits")
    void commonFormats() {
        assertEquals(3,  IfMatchVersionUtil.parseIfMatchVersion("\"3\""));
        assertEquals(7,  IfMatchVersionUtil.parseIfMatchVersion("W/\"7\""));
        assertEquals(12, IfMatchVersionUtil.parseIfMatchVersion("  \"12\"  "));
        assertEquals(1,  IfMatchVersionUtil.parseIfMatchVersion("W/\"001\"")); // leading zeros ok -> 1
    }

    @Test @DisplayName("Garbage or no digits -> 1")
    void garbage() {
        assertEquals(1, IfMatchVersionUtil.parseIfMatchVersion("garbage"));
        assertEquals(1, IfMatchVersionUtil.parseIfMatchVersion("W/\"abc\""));
    }

    @Test @DisplayName("Negative or zero -> 1")
    void nonPositive() {
        assertEquals(1, IfMatchVersionUtil.parseIfMatchVersion("\"-1\""));
        assertEquals(1, IfMatchVersionUtil.parseIfMatchVersion("\"0\""));
    }

    @Test @DisplayName("Bounds: MAX_INT ok, overflow -> 1")
    void bounds() {
        assertEquals(Integer.MAX_VALUE,
            IfMatchVersionUtil.parseIfMatchVersion("W/\"2147483647\""));
        assertEquals(1,
            IfMatchVersionUtil.parseIfMatchVersion("W/\"2147483648\"")); // > Integer.MAX_VALUE
        assertEquals(1,
            IfMatchVersionUtil.parseIfMatchVersion("999999999999999999999999"));
    }
}
