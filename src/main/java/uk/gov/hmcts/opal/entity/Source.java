package uk.gov.hmcts.opal.entity;

import lombok.Getter;

@Getter
public enum Source implements MappingValue {
    ALLPAY("ALLPAY", "allpay"),
    BARCLAYCARD("BARCLAYCARD", "Barclaycard"),
    BRITISH_TELECOM("BTECKOH", "British Telecom"),
    DWP("DWP", "DWP/AEA"),
    NATWEST("NATWEST", "NatWest"),
    OTHER("OTHER", "Other");

    private final String code;
    private final String displayName;

    Source(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
}
