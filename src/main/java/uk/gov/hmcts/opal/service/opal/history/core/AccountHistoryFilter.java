package uk.gov.hmcts.opal.service.opal.history.core;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AccountHistoryFilter {

    LocalDate dateFrom;

    LocalDate dateTo;

    List<AccountHistoryItemType> itemTypes;

    public boolean includes(AccountHistoryItemType itemType) {
        return itemTypes == null || itemTypes.isEmpty() || itemTypes.contains(itemType);
    }
}
