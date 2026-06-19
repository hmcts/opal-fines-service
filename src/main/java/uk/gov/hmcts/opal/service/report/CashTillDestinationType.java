package uk.gov.hmcts.opal.service.report;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.hmcts.opal.entity.DestinationType;

public enum CashTillDestinationType {
    FA,
    SA;

    @JsonCreator
    public static CashTillDestinationType fromValue(String value) {
        return value == null ? null : CashTillDestinationType.valueOf(value);
    }

    public static CashTillDestinationType fromPaymentDestinationType(DestinationType value) {
        return switch (value) {
            case F -> FA;
            case S -> SA;
            default -> throw new IllegalArgumentException("Unsupported Cash Till destination type: " + value);
        };
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
