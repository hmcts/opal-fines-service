package uk.gov.hmcts.opal.authorisation.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
    void whenMinorCreditorMaintenancePermissionRequested_returnsMappedPermission_happyPath() {
        Permission permission = FinesPermission.ACCOUNT_MAINTENANCE_MINOR_CREDITOR.toCommonPermission();

        assertAll(
            () -> assertEquals(20L, permission.getPermissionId()),
            () -> assertEquals("Account Maintenance - Minor Creditor", permission.getPermissionName()));
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
                () -> assertEquals(14L, FinesPermission.OPERATIONAL_REPORT_BY_ENFORCEMENT.getId()),
                () -> assertEquals(
                    "Operational report (by enforcement)",
                    FinesPermission.OPERATIONAL_REPORT_BY_ENFORCEMENT.getDescription()
                )
            ),
            () -> assertAll(
                () -> assertEquals(15L, FinesPermission.OPERATIONAL_REPORT_BY_PAYMENTS.getId()),
                () -> assertEquals(
                    "Operational report (by payment)",
                    FinesPermission.OPERATIONAL_REPORT_BY_PAYMENTS.getDescription()
                )
            )
        );
    }

    @ParameterizedTest
    @MethodSource("newSuspensePermissions")
    void whenNewSuspensePermissionsRequested_returnsConfiguredMetadata_happyPath(
        FinesPermission finesPermission,
        long expectedId,
        String expectedDescription
    ) {
        Permission commonPermission = finesPermission.toCommonPermission();

        assertAll(
            () -> assertEquals(expectedId, finesPermission.getId()),
            () -> assertEquals(expectedDescription, finesPermission.getDescription()),
            () -> assertEquals(expectedId, commonPermission.getPermissionId()),
            () -> assertEquals(expectedDescription, commonPermission.getPermissionName())
        );
    }

    private static Stream<Arguments> newSuspensePermissions() {
        return Stream.of(
            Arguments.of(FinesPermission.VIEW_SUSPENSE_ITEMS, 21L, "View suspense items"),
            Arguments.of(FinesPermission.MANAGE_SUSPENSE_ITEMS, 22L, "Manage suspense items")
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
