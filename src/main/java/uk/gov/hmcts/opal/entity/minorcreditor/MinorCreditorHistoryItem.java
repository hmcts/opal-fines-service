package uk.gov.hmcts.opal.entity.minorcreditor;

import java.time.LocalDateTime;
import java.util.Comparator;
import uk.gov.hmcts.opal.generated.model.MinorCreditorHistoryItemHistory;

public record MinorCreditorHistoryItem(
    MinorCreditorHistoryItemType sourceType,
    Long sourceId,
    LocalDateTime postedDate,
    MinorCreditorHistoryItemHistory responseItem) {

    public static final Comparator<MinorCreditorHistoryItem> ORDERING =
        Comparator.comparing(MinorCreditorHistoryItem::postedDate).reversed()
            .thenComparingInt(item -> item.sourceType().sortOrder())
            .thenComparing(MinorCreditorHistoryItem::sourceId);
}
