package uk.gov.hmcts.opal.entity.minorcreditor;

import java.util.Arrays;
import java.util.stream.Collectors;
import uk.gov.hmcts.opal.generated.model.MinorCreditorHistoryItemHistory;

public enum MinorCreditorHistoryItemType {
    AMENDMENT("amendment", MinorCreditorHistoryItemHistory.TypeEnum.AMENDMENT, 1),
    FINANCIAL("financial", MinorCreditorHistoryItemHistory.TypeEnum.FINANCIAL, 2),
    NOTE("note", MinorCreditorHistoryItemHistory.TypeEnum.NOTE, 3);

    private final String queryValue;
    private final MinorCreditorHistoryItemHistory.TypeEnum responseType;
    private final int sortOrder;

    MinorCreditorHistoryItemType(
        String queryValue,
        MinorCreditorHistoryItemHistory.TypeEnum responseType,
        int sortOrder) {
        this.queryValue = queryValue;
        this.responseType = responseType;
        this.sortOrder = sortOrder;
    }

    public String queryValue() {
        return queryValue;
    }

    public MinorCreditorHistoryItemHistory.TypeEnum responseType() {
        return responseType;
    }

    public int sortOrder() {
        return sortOrder;
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
