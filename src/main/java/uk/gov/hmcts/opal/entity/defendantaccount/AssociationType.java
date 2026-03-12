package uk.gov.hmcts.opal.entity.defendantaccount;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum AssociationType {

    DEFENDANT("Defendant"),
    PARENT_GUARDIAN("Parent/Guardian"),;

    private final String label;

    AssociationType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static AssociationType getByLabel(String label) {
        return Stream.of(AssociationType.values())
            .filter(type -> type.getLabel().equals(normalizeLabel(label)))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    private static String normalizeLabel(String label) {
        return label.replace('_', '/');
    }
}
