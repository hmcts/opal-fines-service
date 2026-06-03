package uk.gov.hmcts.opal.service.opal.history;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryItem;

@Service
public class HistoryItemOrderingService {

    public List<AccountHistoryItem> orderNewestFirst(List<AccountHistoryItem> historyItems) {
        return historyItems.stream()
            .sorted(newestFirstComparator())
            .toList();
    }

    public Comparator<AccountHistoryItem> newestFirstComparator() {
        return Comparator.comparing(
                AccountHistoryItem::getEventDateTime,
                Comparator.nullsLast(Comparator.reverseOrder())
            )
            .thenComparing(AccountHistoryItem::getType, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(AccountHistoryItem::getSourceId, Comparator.nullsLast(Comparator.naturalOrder()));
    }
}
