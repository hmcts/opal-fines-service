package uk.gov.hmcts.opal.service.report;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CashTillPaymentMethod {
    NC,
    PO,
    CQ,
    CT;

    @JsonCreator
    public static CashTillPaymentMethod fromValue(String value) {
        return value == null ? null : CashTillPaymentMethod.valueOf(value);
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
