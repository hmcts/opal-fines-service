package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;

public enum PartyAccountType {
    CREDITOR("Creditor"),
    DEFENDANT("Defendant");

    private final String label;

    PartyAccountType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static PartyAccountType getByLabel(String label) {
        return Stream.of(PartyAccountType.values())
            .filter(v -> v.getLabel().equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown PartyAccountType: " + label));
    }
}
