package uk.gov.hmcts.opal.dto;

public enum RecordType {
    DEFENDANT_ACCOUNTS("defendant_accounts"),
    CREDITOR_ACCOUNTS("creditor_accounts"),
    SUSPENSE_ACCOUNTS("suspense_accounts"),
    REPORT_INSTANCES("report_instances");

    private final String value;

    RecordType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
