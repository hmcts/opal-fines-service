package uk.gov.hmcts.opal.entity.draft;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;

public enum DraftAccountType {

    FINE("Fine"),
    FIXED_PENALTY("Fixed Penalty"),
    CONDITIONAL_CAUTION("Conditional Caution"),
    CONFISCATION("Confiscation");

    private final String label;

    DraftAccountType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static DraftAccountType getByLabel(String label) {
        return Stream.of(DraftAccountType.values())
            .filter(v -> v.getLabel().equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown DraftAccountType: " + label));
    }
}
