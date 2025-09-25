package uk.gov.hmcts.opal.entity.amendment;

public enum RecordType {

    DEFENDANT_ACCOUNTS("defendant_accounts"),
    CREDITOR_ACCOUNTS("creditor_accounts");

    private final String type;

    RecordType(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
