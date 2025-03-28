package uk.gov.hmcts.opal.entity.defendant;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum DefendantAccountType {

    FIXED_PENALTY("Fixed Penalty"),
    FINES("Fines"),
    CONDITIONAL_CAUTION("Conditional Caution"),
    CONFISCATION("Confiscation");

    private final String label;

    DefendantAccountType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static DefendantAccountType getByLabel(String label) {
        return Stream.of(DefendantAccountType.values())
            .filter(c -> c.getLabel().equals(label))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
