package uk.gov.hmcts.opal.common.user.authorisation.model;

public enum Permissions {
    CREATE_MANAGE_DRAFT_ACCOUNTS(1, "Create and Manage Draft Accounts"),
    ACCOUNT_ENQUIRY_NOTES(2, "Account Enquiry - Account Notes"),
    ACCOUNT_ENQUIRY(3, "Account Enquiry"),
    COLLECTION_ORDER(4, "Collection Order"),
    CHECK_VALIDATE_DRAFT_ACCOUNTS(5, "Check and Validate Draft Accounts"),
    SEARCH_AND_VIEW_ACCOUNTS(6, "Search and View Accounts"),
    ACCOUNT_MAINTENANCE(7, "Account Maintenance");

    public static final Permissions[] DRAFT_ACCOUNT_PERMISSIONS = {
        CREATE_MANAGE_DRAFT_ACCOUNTS, CHECK_VALIDATE_DRAFT_ACCOUNTS
    };

    public final long id;

    public final String description;

    Permissions(long id, String description) {
        this.id = id;
        this.description = description;
    }
}
