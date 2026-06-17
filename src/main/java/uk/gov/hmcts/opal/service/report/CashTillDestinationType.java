package uk.gov.hmcts.opal.service.report;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CashTillDestinationType {
    FA,
    SA;

    @JsonCreator
    public static CashTillDestinationType fromValue(String value) {
        return value == null ? null : CashTillDestinationType.valueOf(value);
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
