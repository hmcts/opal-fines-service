package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;

public enum ChequeStatusType {
    DESTROYED("D"),
    NEW("N"),
    PRESENTED("P"),
    QUERY("Q"),
    WITHDRAWN("W"),
    AWAITING_DELETION("X");

    private final String label;

    ChequeStatusType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static ChequeStatusType getByLabel(String label) {
        return Stream.of(ChequeStatusType.values())
            .filter(v -> v.getLabel().equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown ChequeStatusType: " + label));
    }
}
