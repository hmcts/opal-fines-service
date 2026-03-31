package uk.gov.hmcts.opal.entity.document;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum DocumentEntityStatus {
    NEW("New");

    private final String label;

    DocumentEntityStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static DocumentEntityStatus getByLabel(String label) {
        return Stream.of(DocumentEntityStatus.values())
            .filter(v -> v.getLabel().equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown DocumentEntityStatus: " + label));
    }
}
