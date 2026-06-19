package uk.gov.hmcts.opal.dto.history;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum HistoryItemType {

    AMENDMENT("Amendment", "amendment"),
    ENFORCEMENT("Enforcement", "enforcement"),
    FINANCIAL("Financial", "financial"),
    NOTE("Note", "note"),
    PAYMENT_TERMS("Payment terms", "paymentTerms");

    private final String responseValue;
    private final String queryValue;

    HistoryItemType(String responseValue, String queryValue) {
        this.responseValue = responseValue;
        this.queryValue = queryValue;
    }

    @JsonValue
    public String getResponseValue() {
        return responseValue;
    }

    public String getQueryValue() {
        return queryValue;
    }

    @JsonCreator
    public static HistoryItemType fromValue(String value) {
        return Arrays.stream(values())
            .filter(type -> type.responseValue.equalsIgnoreCase(value)
                || type.queryValue.equalsIgnoreCase(value)
                || type.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown history item type: " + value));
    }
}
