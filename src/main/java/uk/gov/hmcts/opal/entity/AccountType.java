package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;

public enum AccountType {
    CREDITOR("Creditor"),
    DEFENDANT("Defendant");

    private final String label;

    AccountType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static AccountType getByLabel(String label) {
        return Stream.of(AccountType.values())
            .filter(v -> v.getLabel().equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown AccountType: " + label));
    }
}
