package uk.gov.hmcts.opal.authorisation.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.opal.common.user.authorisation.model.Permission;

class FinesPermissionTest {

    @ParameterizedTest
    @CsvSource({
        "VIEW_CREDITOR_BACS, 11, View Creditor BACS",
        "OPERATIONAL_REPORT_BY_ENFORCEMENT, 14, Operational report by enforcement",
        "OPERATIONAL_REPORT_BY_PAYMENTS, 15, Operational report by payments",
        "ACCOUNT_MAINTENANCE_MINOR_CREDITOR, 20, Account Maintenance - Minor Creditor",
        "VIEW_SUSPENSE_ITEMS, 24, View Suspense Items",
        "MANAGE_SUSPENSE_ITEMS, 25, Manage Suspense Items"
    })
    void permission_hasExpectedMetadataAndMapping(FinesPermission permission, long id, String description) {
        Permission commonPermission = permission.toCommonPermission();

        assertAll(
            () -> assertEquals(id, permission.getId()),
            () -> assertEquals(description, permission.getDescription()),
            () -> assertEquals(id, commonPermission.getPermissionId()),
            () -> assertEquals(description, commonPermission.getPermissionName()));
    }

    @Test
    void draftAccountPermissions_haveStableOrder() {
        assertArrayEquals(new FinesPermission[] {
            FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS,
            FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS
        }, FinesPermission.DRAFT_ACCOUNT_PERMISSIONS);
    }

    @ParameterizedTest
    @EnumSource(FinesPermission.class)
    void fromString_resolvesNameAndDescription(FinesPermission permission) {
        assertAll(
            () -> assertEquals(permission, FinesPermission.fromString(permission.name())),
            () -> assertEquals(permission, FinesPermission.fromString(permission.getDescription())));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "NOT_A_PERMISSION",
        "View suspense items", "Manage suspense items",
        "view suspense items", "manage suspense items",
        "VIEW SUSPENSE ITEMS", "MANAGE SUSPENSE ITEMS"
    })
    void fromString_rejectsUnknownOrIncorrectCase(String value) {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, () -> FinesPermission.fromString(value));

        assertEquals("Unknown FinesPermission: " + value, exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "  ")
    void fromString_rejectsBlankValue(String value) {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, () -> FinesPermission.fromString(value));

        assertEquals("Permission value cannot be null or blank", exception.getMessage());
    }
}
