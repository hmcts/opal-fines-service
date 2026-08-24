package uk.gov.hmcts.opal.entity.result;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum ResultType {
    ACTION("Action"),
    RESULT("Result");

    private final String label;

    ResultType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static ResultType getByLabel(String label) {
        return Arrays.stream(values())
            .filter(resultType -> resultType.label.equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown ResultType: " + label));
    }
}
