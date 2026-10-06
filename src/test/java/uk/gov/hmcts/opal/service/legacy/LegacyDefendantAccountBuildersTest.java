package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountEnforcementStatusResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountEnforcementStatusResponse.EnforcementAction;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountEnforcementStatusResponse.EnforcementOverview;
import uk.gov.hmcts.opal.dto.legacy.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.legacy.common.CollectionOrder;
import uk.gov.hmcts.opal.dto.legacy.common.CourtReference;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcerReference;
import uk.gov.hmcts.opal.dto.legacy.common.ResultReference;
import uk.gov.hmcts.opal.dto.legacy.common.ResultResponses;
import uk.gov.hmcts.opal.generated.model.AccountStatusReferenceCommon.AccountStatusCodeEnum;
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

    @Test
    void toEnforcementStatusResponse_withNullNextEnforcementActionData() {
        LegacyGetDefendantAccountEnforcementStatusResponse legacyDefendantAccountStatus =
            LegacyGetDefendantAccountEnforcementStatusResponse.builder()
                .version(BigInteger.ONE)
                .enforcementOverview(EnforcementOverview.builder().daysInDefault(10).collectionOrder(
                    CollectionOrder.builder().build()).enforcementCourt(CourtReference.builder()
                    .courtId(1234L).build()).build())
                .accountStatusReference(AccountStatusReference.builder()
                    .accountStatusCode("WO")
                    .accountStatusDisplayName("WO")
                    .build())
                .employerFlag("true")
                .build();

        EnforcementStatus result = LegacyDefendantAccountBuilders
            .toEnforcementStatusResponse(legacyDefendantAccountStatus, null);

        assertNotNull(result);
        assertEquals(BigInteger.ONE, result.getVersion());
        assertEquals(1234L, result.getEnforcementOverview().getEnforcementCourt().getCourtId());
        assertEquals(10, result.getEnforcementOverview().getDaysInDefault());
        assertEquals(AccountStatusCodeEnum.WO, result.getAccountStatusReference().getAccountStatusCode());
        assertTrue(result.getEmployerFlag());
        assertNull(result.getNextEnforcementActionData());
    }

    @Test
    void toEnforcementStatusResponse_withValidNextEnforcementActionData() {
        LegacyGetDefendantAccountEnforcementStatusResponse legacyDefendantAccountStatus =
            LegacyGetDefendantAccountEnforcementStatusResponse.builder()
                .version(BigInteger.ONE)
                .enforcementOverview(EnforcementOverview.builder().daysInDefault(10).collectionOrder(
                    CollectionOrder.builder().build()).enforcementCourt(CourtReference.builder()
                    .courtId(1234L).build()).build())
                .accountStatusReference(AccountStatusReference.builder()
                    .accountStatusCode("WO")
                    .accountStatusDisplayName("WO")
                    .build())
                .employerFlag("true")
                .build();

        EnforcementStatus result = LegacyDefendantAccountBuilders
            .toEnforcementStatusResponse(legacyDefendantAccountStatus, "All");

        assertNotNull(result);
        assertEquals(BigInteger.ONE, result.getVersion());
        assertEquals(1234L, result.getEnforcementOverview().getEnforcementCourt().getCourtId());
        assertEquals(10, result.getEnforcementOverview().getDaysInDefault());
        assertEquals(AccountStatusCodeEnum.WO, result.getAccountStatusReference().getAccountStatusCode());
        assertTrue(result.getEmployerFlag());
        assertEquals("All", result.getNextEnforcementActionData());
    }

    private EnforcementAction enforcementActionWithDateAdded(String dateAdded) {
        return EnforcementAction.builder()
            .dateAdded(dateAdded)
            .enforcer(EnforcerReference.builder().enforcerId(1L).enforcerName("Test Enforcer").build())
            .resultReference(ResultReference.builder().resultId("REM").resultTitle("Reminder").build())
            .build();
    }
}
