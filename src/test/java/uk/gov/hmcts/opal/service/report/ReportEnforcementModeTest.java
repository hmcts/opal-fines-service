package uk.gov.hmcts.opal.service.report;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ReportEnforcementModeTest {

    @Test
    void from_returnsAllWhenValueIsNull() {
        assertEquals(ReportEnforcementMode.ALL, ReportEnforcementMode.from(null));
    }

    @Test
    void from_returnsMatchingEnumWhenValueIsValid() {
        assertEquals(ReportEnforcementMode.ALL, ReportEnforcementMode.from("ALL"));
        assertEquals(ReportEnforcementMode.LAST_ACTION, ReportEnforcementMode.from("LAST_ACTION"));
        assertEquals(ReportEnforcementMode.REGF, ReportEnforcementMode.from("REGF"));
        assertEquals(ReportEnforcementMode.NOT_UNDER_ENFORCEMENT, ReportEnforcementMode
            .from("NOT_UNDER_ENFORCEMENT"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "UNKNOWN"})
    void from_throwsExceptionWhenValueIsUnknown(String value) {
        assertThrows(IllegalArgumentException.class, () -> ReportEnforcementMode.from(value));
    }
}