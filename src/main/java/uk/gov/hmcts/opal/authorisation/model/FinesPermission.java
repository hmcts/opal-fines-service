package uk.gov.hmcts.opal.authorisation.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.opal.common.user.authorisation.model.PermissionDescriptor;

/**
 * Fines-service specific permission catalogue. Each entry mirrors the ids maintained by
 * the user service so that {@link uk.gov.hmcts.opal.common.user.authorisation.model.UserState}
 * can be queried using the shared {@link PermissionDescriptor} contract.
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
    AMEND_PAYMENT_TERMS(9L, "Amend Payment Terms"),
    ENTER_ENFORCEMENT(10L, "Enter Enforcement"),
    CONSOLIDATE(13L, "Consolidate");

    /**
     * Convenience aggregate used by parts of the service that require both draft account
     * permissions.
     */
    public static final FinesPermission[] DRAFT_ACCOUNT_PERMISSIONS = {
        CREATE_MANAGE_DRAFT_ACCOUNTS, CHECK_VALIDATE_DRAFT_ACCOUNTS
    };

    private final long id;
    private final String description;
}
