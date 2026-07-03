package uk.gov.hmcts.opal.entity.paymentterms;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;

public enum InstalmentPeriod {
    FORTNIGHT("F", "fortnightly"),
    MONTH("M", "monthly"),
    WEEK("W", "weekly");

    private final String code;

    private final String reportText;

    InstalmentPeriod(String code, String reportText) {
        this.code = code;
        this.reportText = reportText;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonIgnore
    public String getReportText() {
        return reportText;
    }

    public static InstalmentPeriod fromCode(String code) {
        return Stream.of(InstalmentPeriod.values())
            .filter(v -> v.getCode().equals(code))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown InstalmentPeriod code: " + code));
    }
}
