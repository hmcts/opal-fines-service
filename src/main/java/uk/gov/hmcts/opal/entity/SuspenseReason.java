package uk.gov.hmcts.opal.entity;

import lombok.Getter;

@Getter
public enum SuspenseReason implements MappingValue {
    FINES_OVERPAYMENT("OF", "Fines overpayment"),
    FINE_PAID_IN_ADVANCE("FA", "Fine paid in advance"),
    MISC_PAYMENT("MS", "Miscellaneous payment"),
    UNIDENTIFIED_AMOUNT("UN", "Unidentified Amount");

    private final String code;
    private final String displayName;

    SuspenseReason(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
}
