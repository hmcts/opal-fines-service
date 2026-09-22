package uk.gov.hmcts.opal.entity.alternatepaymentreference;

import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum Category {
    APR("APR"),
    ACC("ACC");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public static Category getByLabel(String label) {
        return Stream.of(Category.values())
            .filter(type -> type.getLabel().equals(label))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
