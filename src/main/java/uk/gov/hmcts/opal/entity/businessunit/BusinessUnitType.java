package uk.gov.hmcts.opal.entity.businessunit;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum BusinessUnitType {
    ACCOUNTING_DIViSION("Accounting Division"),
    AREA("Area");

    private final String label;

    BusinessUnitType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static BusinessUnitType getByLabel(String label) {
        return Stream.of(BusinessUnitType.values())
            .filter(type -> type.getLabel().equals(label))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
