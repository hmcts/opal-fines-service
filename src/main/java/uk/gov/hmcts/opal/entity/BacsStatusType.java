package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;

public enum BacsStatusType {
    TRANSFERRED("T"),
    REISSUED("R"),
    CANCELLED_CHEQUE_WRITTEN("C"),
    CANCELLED_POSTED_TO_SUSPENSE("S"),
    CANCELLED_POSTED_TO_CENTRAL_FUND("F"),
    CLEARED_AWAITING_DELETION("X"),
    UNPROCESSED("U");

    private final String label;

    BacsStatusType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static BacsStatusType getByLabel(String label) {
        return Stream.of(BacsStatusType.values())
            .filter(v -> v.getLabel().equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown BACS Status Type: " + label));
    }
}
