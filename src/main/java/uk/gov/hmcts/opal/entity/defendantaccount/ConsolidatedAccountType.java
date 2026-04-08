package uk.gov.hmcts.opal.entity.defendantaccount;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;

public enum ConsolidatedAccountType {
    MASTER("M"),
    CHILD("C");

    private final String label;

    ConsolidatedAccountType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static ConsolidatedAccountType getByLabel(String label) {
        return Stream.of(values())
            .filter(type -> type.getLabel().equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown ConsolidatedAccountType: " + label));
    }
}
