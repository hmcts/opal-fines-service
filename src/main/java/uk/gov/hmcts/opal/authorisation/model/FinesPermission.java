package uk.gov.hmcts.opal.authorisation.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.opal.common.user.authorisation.model.Permission;
import uk.gov.hmcts.opal.common.user.authorisation.model.PermissionDescriptor;

/**
 * Fines-service specific permission catalogue. Each entry mirrors the ids maintained by the user service so that
 * {@link uk.gov.hmcts.opal.common.user.authorisation.model.UserState} can be queried using the shared
 * {@link PermissionDescriptor} contract.
 */
@Getter
@RequiredArgsConstructor
public enum FinesPermission implements PermissionDescriptor {
    CREATE_MANAGE_DRAFT_ACCOUNTS(1L, "Create and Manage Draft Accounts"),
    ACCOUNT_ENQUIRY_NOTES(2L, "Account Enquiry - Account Notes"),
    ACCOUNT_ENQUIRY(3L, "Account Enquiry"),
    COLLECTION_ORDER(4L, "Collection Order"),
    CHECK_VALIDATE_DRAFT_ACCOUNTS(5L, "Check and Validate Draft Accounts"),
    SEARCH_AND_VIEW_ACCOUNTS(6L, "Search and View Accounts"),
    ACCOUNT_MAINTENANCE(7L, "Account Maintenance"),
    VIEW_CREDITOR_BACS(11L, "View Creditor BACS"),
    AMEND_PAYMENT_TERMS(9L, "Amend Payment Terms"),
    ENTER_ENFORCEMENT(10L, "Enter Enforcement"),
    CONSOLIDATE(13L, "Consolidate"),
    ADD_AND_REMOVE_PAYMENT_HOLD(14L, "Add and Remove payment hold"),
    AUTO_ENFORCEMENT(15L, "Auto Enforcement");

    /**
     * Convenience aggregate used by parts of the service that require both draft account permissions.
     */
    public static final FinesPermission[] DRAFT_ACCOUNT_PERMISSIONS = {
        CREATE_MANAGE_DRAFT_ACCOUNTS, CHECK_VALIDATE_DRAFT_ACCOUNTS
    };
    private final long id;
    private final String description;


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

    public Permission toCommonPermission() {
        return Permission.builder()
            .permissionId(id)
            .permissionName(description)
            .build();
    }
}
