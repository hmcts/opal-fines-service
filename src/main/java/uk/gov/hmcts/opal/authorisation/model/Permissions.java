package uk.gov.hmcts.opal.authorisation.model;

public enum Permissions {
    ACCOUNT_ENQUIRY(54, "Account Enquiry"),
    ACCOUNT_ENQUIRY_NOTES(41, "Account Enquiry - Account Notes"),
    MANUAL_ACCOUNT_CREATION(35, "Manual Account Creation");

    public final long id;

    public final String description;

    Permissions(long id, String description) {
        this.id = id;
        this.description = description;
    }
}
