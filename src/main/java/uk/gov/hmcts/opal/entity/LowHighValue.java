package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;

public enum LowHighValue {
    LOW("L"),
    HIGH("H");

    private final String value;

    LowHighValue(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static LowHighValue getByValue(String value) {
        return Stream.of(LowHighValue.values())
            .filter(acp -> acp.getValue().equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown LowHighValue: " + value));
    }
}
