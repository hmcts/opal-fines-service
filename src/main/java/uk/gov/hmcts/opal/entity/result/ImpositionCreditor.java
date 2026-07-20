package uk.gov.hmcts.opal.entity.result;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum ImpositionCreditor {
    ANY("Any"),
    CF("CF"),
    NOT_CPS("!CPS"),
    CPS("CPS");

    private final String label;

    ImpositionCreditor(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static ImpositionCreditor getByLabel(String label) {
        return Arrays.stream(values())
            .filter(impositionCreditor -> impositionCreditor.label.equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown ImpositionCreditor: " + label));
    }
}
