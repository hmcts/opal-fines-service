package uk.gov.hmcts.opal.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NamesUtilTest {

    @Test
    void testSplitName() {
        String[] result = NamesUtil.splitFullName(" Smith , Mr JJ");
        assertEquals("Smith", result[0]);
        assertEquals("Mr", result[1]);
        assertEquals("JJ", result[2]);
    }
}
