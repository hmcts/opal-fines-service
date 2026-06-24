package uk.gov.hmcts.opal.entity.enforcement;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnforcementAccountTypeExtended {

    ADULT_COLLECTION_ORDER_LOW("COLL"),
    ADULT_COLLECTION_ORDER_HIGH("COLH"),
    ADULT_NO_COLLECTION_ORDER_LOW("AL"),
    ADULT_NO_COLLECTION_ORDER_HIGH("AH"),
    COMPANY_LOW("COL"),
    COMPANY_HIGH("COH"),
    YOUTH_LOW("YL"),
    YOUTH_HIGH("YH"),
    CFPL("CFPL"),
    CFPH("CFPH"),
    TFOL("TFOL"),
    TFOH("TFOH"),
    CCL("CCL"),
    CCH("CCH"),
    FPVL("FPVL"),
    FPVH("FPVH"),
    FPNL("FPNL"),
    FPNH("FPNH"),
    LAL("LAL"),
    LAH("LAH"),
    COMP("COMP");

    private final String code;


    public static EnforcementAccountTypeExtended fromCode(String code) {
        for (EnforcementAccountTypeExtended value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
