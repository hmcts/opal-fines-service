package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;
import java.util.stream.Stream;

public enum AssociatedRecordType {
    DEFENDANT_ACCOUNTS,
    CREDITOR_TRANSACTIONS,
    MISCELLANEOUS_ACCOUNTS,
    CREDITOR_ACCOUNTS,
    SUSPENSE_TRANSACTIONS,
    SUSPENSE_ITEMS,
    ENFORCEMENTS,
    CHEQUES,
    IMPOSITIONS,
    DEFENDANT_TRANSACTIONS,
    REPORT_INSTANCES;

    @JsonValue
    public String getLabel() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static AssociatedRecordType getByLabel(String label) {
        return Stream.of(values())
            .filter(value -> value.getLabel().equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown AssociatedRecordType: " + label));
    }

    @Override
    public String toString() {
        return getLabel();
    }
}
