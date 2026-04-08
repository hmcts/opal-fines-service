package uk.gov.hmcts.opal.entity.defendantaccount;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;

public enum OriginatorType {
    MAC_NEW_ACCOUNT("NEW"),
    FIXED_PENALTY("FP"),
    TRANSFER_IN_ACCOUNT("TFO");

    private final String label;

    OriginatorType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static OriginatorType getByLabel(String label) {
        return Stream.of(values())
            .filter(type -> type.getLabel().equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown OriginatorType: " + label));
    }
}
