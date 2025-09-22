package uk.gov.hmcts.opal.common.user.authorisation.model;

public enum LogActions {

    LOG_IN(5000, "Log In"),
    LOG_OUT(5001, "Log Out"),
    ACCOUNT_ENQUIRY(5002, "Account Notes"),
    ACCOUNT_ENQUIRY_NOTES(5003, "Account Enquiry - Account Notes");

    public final short id;

    public final String name;

    LogActions(int id, String name) {
        this.id = (short)id;
        this.name = name;
    }


}
