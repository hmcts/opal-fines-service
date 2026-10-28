package uk.gov.hmcts.opal.authorisation.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.opal.common.user.authorisation.model.Permission;
import uk.gov.hmcts.opal.common.user.authorisation.model.PermissionDescriptor;
import uk.gov.hmcts.opal.common.user.authorisation.model.PermissionDescriptorV2;
import uk.gov.hmcts.opal.common.user.authorisation.model.PermissionV2;

/**
 * Fines-service specific permission catalogue. Each entry mirrors the codes maintained by the user service so that
 * {@link uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2} can be queried using the shared
 * {@link PermissionDescriptor} contract.
 */
@Getter
@RequiredArgsConstructor
public enum FinesPermission implements PermissionDescriptorV2 {
    CREATE_MANAGE_DRAFT_ACCOUNTS("CREATE_MANAGE_DRAFT_ACCOUNTS", "Create Manage Draft Accounts"),
    ACCOUNT_ENQUIRY_NOTES("ACCOUNT_ENQUIRY_NOTES", "Account Enquiry - Account Notes"),
    ACCOUNT_ENQUIRY("ACCOUNT_ENQUIRY", "Account Enquiry"),
    COLLECTION_ORDER("COLLECTION_ORDER", "Collection Order"),
    CHECK_VALIDATE_DRAFT_ACCOUNTS("CHECK_VALIDATE_DRAFT_ACCOUNTS", "Check and Validate Draft Accounts"),
    SEARCH_AND_VIEW_ACCOUNTS("SEARCH_AND_VIEW_ACCOUNTS", "Search and View Accounts"),
    ACCOUNT_MAINTENANCE("ACCOUNT_MAINTENANCE", "Account Maintenance"),
    ADD_ACCOUNT_ACTIVITY_NOTES("ADD_ACCOUNT_ACTIVITY_NOTES", "Add account activity notes"),
    AMEND_PAYMENT_TERMS("AMEND_PAYMENT_TERMS", "Amend Payment Terms"),
    ENTER_ENFORCEMENT("ENTER_ENFORCEMENT", "Enter Enforcement"),
    VIEW_CREDITOR_BACS("VIEW_CREDITOR_BACS", "View Creditor BACS"),
    CONSOLIDATE("CONSOLIDATE", "Consolidate"),
    ADD_AND_REMOVE_PAYMENT_HOLD("ADD_AND_REMOVE_PAYMENT_HOLD", "Add and Remove payment hold"),
    PROCESS_AND_ALLOCATE_PAYMENTS("PROCESS_AND_ALLOCATE_PAYMENTS", "Process and Allocate Payments"),
    AUTO_ENFORCEMENT("AUTO_ENFORCEMENT", "Auto Enforcement"),
    OPERATIONAL_REPORT_BY_ENFORCEMENT(
        "OPERATIONAL_REPORT_BY_ENFORCEMENT", "Operational report by enforcement"),
    OPERATIONAL_REPORT_BY_PAYMENTS("OPERATIONAL_REPORT_BY_PAYMENTS", "Operational report by payments"),
    ACCOUNT_MAINTENANCE_MINOR_CREDITOR(
        "ACCOUNT_MAINTENANCE_MINOR_CREDITOR", "Account Maintenance - Minor Creditor");

    /**
     * Convenience aggregate used by parts of the service that require both draft account permissions.
     */
    public static final FinesPermission[] DRAFT_ACCOUNT_PERMISSIONS = {
        CREATE_MANAGE_DRAFT_ACCOUNTS, CHECK_VALIDATE_DRAFT_ACCOUNTS
    };
    private final String permissionCode;
    private final String permissionName;

    public static FinesPermission fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Permission value cannot be null or blank");
        }
        try {
            return FinesPermission.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown FinesPermission: " + value, e);
        }
    }

    public PermissionV2 toCommonPermission() {
        return PermissionV2.fromPermissionCode(permissionCode);
    }
}
