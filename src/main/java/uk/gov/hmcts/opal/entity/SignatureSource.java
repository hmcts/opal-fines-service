package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;

public enum SignatureSource {
    AREA("Area"),
    LJA("LJA");

    private final String label;

    SignatureSource(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static SignatureSource getByLabel(String label) {
        return Stream.of(SignatureSource.values())
            .filter(v -> v.getLabel().equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown SignatureSource: " + label));
    }
}
