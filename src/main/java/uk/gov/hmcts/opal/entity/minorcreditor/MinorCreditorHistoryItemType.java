package uk.gov.hmcts.opal.entity.minorcreditor;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum MinorCreditorHistoryItemType {
    AMENDMENT("amendment"),
    FINANCIAL("financial"),
    NOTE("note");

    private final String queryValue;

    MinorCreditorHistoryItemType(String queryValue) {
        this.queryValue = queryValue;
    }

    public String queryValue() {
        return queryValue;
    }

    public static MinorCreditorHistoryItemType fromQueryValue(String queryValue) {
        return Arrays.stream(values())
            .filter(itemType -> itemType.queryValue.equals(queryValue))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "itemTypes must contain only " + allowedValues()));
    }

    public static String allowedValues() {
        return Arrays.stream(values())
            .map(MinorCreditorHistoryItemType::queryValue)
            .collect(Collectors.joining(", "));
    }
}
