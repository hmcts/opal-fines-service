package uk.gov.hmcts.opal.authorisation.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.opal.common.user.authorisation.model.Permission;

class FinesPermissionTest {

    @Test
    void whenCommonPermissionRequested_returnsMappedPermission_happyPath() {
        Permission permission = FinesPermission.VIEW_CREDITOR_BACS.toCommonPermission();

        assertAll(
            () -> assertEquals(11L, permission.getPermissionId()),
            () -> assertEquals("View Creditor BACS", permission.getPermissionName())
        );
    }

    @Test
    void whenDraftAccountPermissionsRequested_returnsStableOrder_happyPath() {
        assertArrayEquals(
            new FinesPermission[] {
                FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS,
                FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS
            },
            FinesPermission.DRAFT_ACCOUNT_PERMISSIONS
        );
    }

    @Test
    void whenOperationalReportPermissionsRequested_returnsConfiguredMetadata_happyPath() {
        assertAll(
            () -> assertAll(
                () -> assertEquals(18L, FinesPermission.OPERATIONAL_REPORT_ENFORCEMENT.getId()),
                () -> assertEquals(
                    "OPERATIONAL_REPORT_ENFORCEMENT",
                    FinesPermission.OPERATIONAL_REPORT_ENFORCEMENT.getDescription()
                )
            ),
            () -> assertAll(
                () -> assertEquals(19L, FinesPermission.OPERATIONAL_REPORT_PAYMENT.getId()),
                () -> assertEquals(
                    "OPERATIONAL_REPORT_PAYMENT",
                    FinesPermission.OPERATIONAL_REPORT_PAYMENT.getDescription()
                )
            )
        );
    }

    @Nested
    class FromString {

        @Test
        void whenPermissionNameProvided_returnsMatchingEnum_happyPath() {
            assertEquals(
                FinesPermission.SEARCH_AND_VIEW_ACCOUNTS,
                FinesPermission.fromString("SEARCH_AND_VIEW_ACCOUNTS")
            );
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "  "})
        void whenBlankPermissionNameProvided_throwsException_sadPath(String value) {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> FinesPermission.fromString(value)
            );

            assertEquals("Permission value cannot be null or blank", exception.getMessage());
        }

        @Test
        void whenUnknownPermissionNameProvided_throwsException_sadPath() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> FinesPermission.fromString("NOT_A_PERMISSION")
            );

            assertEquals("Unknown FinesPermission: NOT_A_PERMISSION", exception.getMessage());
        }
    }
}
