package uk.gov.hmcts.opal.entity.enforcement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.opal.dto.EnforcementAccountTypeExtended;

@RequiredArgsConstructor
public enum EnforcementAccountType {
    COLL("COLL"),
    COLH("COLH"),
    AL("AL"),
    AH("AH"),
    COL("COL"),
    COH("COH"),
    YL("YL"),
    YH("YH");

    @Getter
    private final String code;

    public static EnforcementAccountType fromCode(String code) {
        for (EnforcementAccountType value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
