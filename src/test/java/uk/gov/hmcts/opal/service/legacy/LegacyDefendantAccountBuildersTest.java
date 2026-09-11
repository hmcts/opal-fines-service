package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountEnforcementStatusResponse.EnforcementAction;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcerReference;
import uk.gov.hmcts.opal.dto.legacy.common.ResultReference;
import uk.gov.hmcts.opal.dto.legacy.common.ResultResponses;
import uk.gov.hmcts.opal.generated.model.EnforcementActionDefendantAccount;

class LegacyDefendantAccountBuildersTest {

    @Test
    void buildEnforcementActionDefendantAccount_parsesDateTimeDateAdded() {
        EnforcementAction action = enforcementActionWithDateAdded("2024-01-01T10:00:00");

        LocalDateTime result = LegacyDefendantAccountBuilders
            .buildEnforcementActionDefendantAccount(action)
            .getDateAdded();

        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), result);
    }

    @Test
    void buildEnforcementActionDefendantAccount_parsesDateOnlyDateAddedAtStartOfDay() {
        EnforcementAction action = enforcementActionWithDateAdded("2026-08-24");

        LocalDateTime result = LegacyDefendantAccountBuilders
            .buildEnforcementActionDefendantAccount(action)
            .getDateAdded();

        assertEquals(LocalDateTime.of(2026, 8, 24, 0, 0), result);
    }

    @Test
    void buildEnforcementActionDefendantAccount_returnsNullDateAddedWhenBlank() {
        EnforcementAction action = enforcementActionWithDateAdded(" ");

        LocalDateTime result = LegacyDefendantAccountBuilders
            .buildEnforcementActionDefendantAccount(action)
            .getDateAdded();

        assertNull(result);
    }

    @Test
    void buildEnforcementActionDefendantAccount_returnsNullWhenOptionalFieldsAreMissingOrBlank() {
        EnforcementAction action = EnforcementAction.builder()
            .resultReference(ResultReference.builder().resultId(" ").build())
            .build();

        EnforcementActionDefendantAccount result =
            LegacyDefendantAccountBuilders.buildEnforcementActionDefendantAccount(action);

        assertNull(result);
    }

    @Test
    void buildEnforcementActionDefendantAccount_mapsPartialActionWithoutDateAdded() {
        EnforcementAction action = EnforcementAction.builder()
            .reason("late")
            .resultReference(ResultReference.builder().resultId(" ").build())
            .build();

        EnforcementActionDefendantAccount result =
            LegacyDefendantAccountBuilders.buildEnforcementActionDefendantAccount(action);

        assertNotNull(result);
        assertEquals("late", result.getReason());
        assertNull(result.getDateAdded());
        assertNull(result.getEnforcementAction());
    }

    @Test
    void buildEnforcementActionDefendantAccount_ignoresBlankNestedOptionalValues() {
        EnforcementAction action = EnforcementAction.builder()
            .warrantNumber("123")
            .enforcer(EnforcerReference.builder().enforcerName(" ").build())
            .resultResponses(ResultResponses.builder().parameterName(" ").response(" ").build())
            .build();

        EnforcementActionDefendantAccount result =
            LegacyDefendantAccountBuilders.buildEnforcementActionDefendantAccount(action);

        assertNotNull(result);
        assertEquals("123", result.getWarrantNumber());
        assertNull(result.getEnforcer());
        assertNull(result.getResultResponses());
    }

    private EnforcementAction enforcementActionWithDateAdded(String dateAdded) {
        return EnforcementAction.builder()
            .dateAdded(dateAdded)
            .enforcer(EnforcerReference.builder().enforcerId(1L).enforcerName("Test Enforcer").build())
            .resultReference(ResultReference.builder().resultId("REM").resultTitle("Reminder").build())
            .build();
    }
}
