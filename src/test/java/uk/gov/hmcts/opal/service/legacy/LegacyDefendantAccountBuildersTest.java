package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountEnforcementStatusResponse.EnforcementAction;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcerReference;
import uk.gov.hmcts.opal.dto.legacy.common.ResultReference;
import uk.gov.hmcts.opal.dto.legacy.common.ResultResponses;
import uk.gov.hmcts.opal.generated.model.EnforcementActionDefendantAccount;

class LegacyDefendantAccountBuildersTest {

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
}
