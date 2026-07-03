package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;

public enum ChequeAllocationType {
    COMPENSATION("COMP"),
    REPAY_WITNESS_EXPENSES("REPAYW");

    private final String label;

    ChequeAllocationType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static ChequeAllocationType getByLabel(String label) {
        return Stream.of(ChequeAllocationType.values())
            .filter(v -> v.getLabel().equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown ChequeAllocationType: " + label));
    }
}
