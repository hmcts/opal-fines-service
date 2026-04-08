package uk.gov.hmcts.opal.entity.paymentterms;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;

public enum InstalmentPeriod {
    FORTNIGHT("F"),
    MONTH("M"),
    WEEK("W");

    private final String code;

    InstalmentPeriod(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public static InstalmentPeriod fromCode(String code) {
        return Stream.of(InstalmentPeriod.values())
            .filter(v -> v.getCode().equals(code))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown InstalmentPeriod code: " + code));
    }
}
