package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountEnforcementStatusResponse.EnforcementAction;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcerReference;
import uk.gov.hmcts.opal.dto.legacy.common.ResultReference;

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

    private EnforcementAction enforcementActionWithDateAdded(String dateAdded) {
        return EnforcementAction.builder()
            .dateAdded(dateAdded)
            .enforcer(EnforcerReference.builder().enforcerId(1L).enforcerName("Test Enforcer").build())
            .resultReference(ResultReference.builder().resultId("REM").resultTitle("Reminder").build())
            .build();
    }
}
