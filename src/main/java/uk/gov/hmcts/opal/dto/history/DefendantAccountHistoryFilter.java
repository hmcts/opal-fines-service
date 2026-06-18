package uk.gov.hmcts.opal.dto.history;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DefendantAccountHistoryFilter {

    LocalDate dateFrom;

    LocalDate dateTo;

    List<HistoryItemType> itemTypes;

    public boolean includes(HistoryItemType itemType) {
        return itemTypes == null || itemTypes.isEmpty() || itemTypes.contains(itemType);
    }
}
