package uk.gov.hmcts.opal.authorisation.model;

public enum Permissions {
    CREATE_MANAGE_DRAFT_ACCOUNTS(35, "Create and Manage Draft Accounts"),
    ACCOUNT_ENQUIRY(54, "Account Enquiry"),
    ACCOUNT_ENQUIRY_NOTES(41, "Account Enquiry - Account Notes"),
    COLLECTION_ORDER(500, "Collection Order"),
    CHECK_VALIDATE_DRAFT_ACCOUNTS(501, "Check and Validate Draft Accounts"),
    SEARCH_AND_VIEW_ACCOUNTS(502, "Search and view accounts");
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
