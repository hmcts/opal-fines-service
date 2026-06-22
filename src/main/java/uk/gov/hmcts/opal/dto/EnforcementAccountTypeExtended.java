package uk.gov.hmcts.opal.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnforcementAccountTypeExtended {

    COLL("COLL"),
    COLH("COLH"),
    AL("AL"),
    AH("AH"),
    COL("COL"),
    COH("COH"),
    YL("YL"),
    YH("YH"),
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
