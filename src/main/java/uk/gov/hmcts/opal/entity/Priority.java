package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;

public enum Priority {
    ZERO("0"),
    ONE("1"),
    TWO("2");

    private final String label;

    Priority(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static Priority getByLabel(String label) {
        return Stream.of(Priority.values())
            .filter(v -> v.getLabel().equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown Priority: " + label));
    }
}
