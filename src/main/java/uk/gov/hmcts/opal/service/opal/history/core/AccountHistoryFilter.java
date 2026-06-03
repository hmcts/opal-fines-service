package uk.gov.hmcts.opal.service.opal.history.core;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;

@Value
@Builder
public class AccountHistoryFilter {

    LocalDate dateFrom;

    LocalDate dateTo;

    List<HistoryItemType> itemTypes;

    public boolean includes(HistoryItemType itemType) {
        return itemTypes == null || itemTypes.isEmpty() || itemTypes.contains(itemType);
    }
}
