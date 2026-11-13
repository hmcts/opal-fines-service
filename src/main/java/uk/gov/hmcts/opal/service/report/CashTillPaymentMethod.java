package uk.gov.hmcts.opal.service.report;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CashTillPaymentMethod {
    NC("Notes and coins"),
    PO("Postal order"),
    CQ("Cheque"),
    CT("Credit transfer");

    private final String displayName;

    CashTillPaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public static CashTillPaymentMethod fromValue(String value) {
        return value == null ? null : CashTillPaymentMethod.valueOf(value);
    }

    @JsonCreator
    public static CashTillPaymentMethod fromDisplayLabel(String value) {
        for (CashTillPaymentMethod type : CashTillPaymentMethod.values()) {
            if (type.displayName.equals(value)) {
                return type;
            }
        }
        return null;
    }

    @JsonValue
    public String toDisplayName() {
        return displayName;
    }

    public String toValue() {
        return name();
    }
}
