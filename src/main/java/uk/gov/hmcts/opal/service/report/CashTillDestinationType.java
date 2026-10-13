package uk.gov.hmcts.opal.service.report;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.hmcts.opal.entity.DestinationType;

public enum CashTillDestinationType {
    FA("Fines Account"),
    SA("Suspense Account");

    private final String displayName;

    CashTillDestinationType(String label) {
        this.displayName = label;
    }

    @JsonCreator
    public static CashTillDestinationType fromDisplayLabel(String value) {
        for (CashTillDestinationType type : CashTillDestinationType.values()) {
            if (type.displayName.equals(value)) {
                return type;
            }
        }
        return null;
    }

    public static CashTillDestinationType fromPaymentDestinationType(DestinationType value) {
        return switch (value) {
            case F -> FA;
            case S -> SA;
            default -> throw new IllegalArgumentException("Unsupported Cash Till destination type: " + value);
        };
    }

    @JsonValue
    public String toDisplayName() {
        return displayName;
    }

    public String toValue() {
        return name();
    }
}
