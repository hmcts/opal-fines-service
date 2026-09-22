package uk.gov.hmcts.opal.entity.alternatepaymentreference;

import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum Relationship {
    APR("APR"),
    CONSOLIDATED("CONSOLIDATED"),
    AMALGAMATED("AMALGAMATED");

    private final String label;

    Relationship(String label) {
        this.label = label;
    }

    public static Relationship getByLabel(String label) {
        return Stream.of(Relationship.values())
            .filter(type -> type.getLabel().equals(label))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
