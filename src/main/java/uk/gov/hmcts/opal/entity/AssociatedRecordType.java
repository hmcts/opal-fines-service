package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum AssociatedRecordType {
    DEFENDANT_ACCOUNTS("defendant_accounts"),
    CREDITOR_TRANSACTIONS("creditor_transactions"),
    MISCELLANEOUS_ACCOUNTS("miscellaneous_accounts"),
    CREDITOR_ACCOUNTS("creditor_accounts"),
    SUSPENSE_TRANSACTIONS("suspense_transactions"),
    SUSPENSE_ITEMS("suspense_items"),
    ENFORCEMENTS("enforcements"),
    CHEQUES("cheques"),
    IMPOSITIONS("impositions"),
    DEFENDANT_TRANSACTIONS("defendant_transactions"),
    REPORT_INSTANCES("report_instances");

    private final String label;

    AssociatedRecordType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static AssociatedRecordType getByLabel(String label) {
        return Stream.of(values())
            .filter(value -> value.getLabel().equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown AssociatedRecordType: " + label));
    }

    @Override
    public String toString() {
        return label;
    }
}
