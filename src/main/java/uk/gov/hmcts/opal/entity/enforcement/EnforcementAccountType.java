package uk.gov.hmcts.opal.entity.enforcement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EnforcementAccountType {
    ADULT_COLLECTION_ORDER_LOW("COLL"),
    ADULT_COLLECTION_ORDER_HIGH("COLH"),
    ADULT_NO_COLLECTION_ORDER_LOW("AL"),
    ADULT_NO_COLLECTION_ORDER_HIGH("AH"),
    COMPANY_LOW("COL"),
    COMPANY_HIGH("COH"),
    YOUTH_LOW("YL"),
    YOUTH_HIGH("YH");

    @Getter
    private final String code;

    public static EnforcementAccountType getByCode(String code) {
        for (EnforcementAccountType value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
