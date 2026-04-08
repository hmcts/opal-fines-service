package uk.gov.hmcts.opal.entity.paymentterms;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum TermsTypeCode {
    BY_DATE("B"),
    INSTALMENTS("I"),
    PAID("P");

    private final String code;

    TermsTypeCode(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static TermsTypeCode fromCode(String code) {
        return Stream.of(TermsTypeCode.values())
            .filter(v -> v.getCode().equals(code))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown TermsTypeCode code: " + code));
    }
}
