package uk.gov.hmcts.opal.service.opal.history;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;

@Service
public class HistoryItemOrderingService {

    public List<DefendantAccountHistoryItem> orderNewestFirst(List<DefendantAccountHistoryItem> historyItems) {
        return historyItems.stream()
            .sorted(newestFirstHistoryItemComparator())
            .toList();
    }

    private Comparator<DefendantAccountHistoryItem> newestFirstHistoryItemComparator() {
        return Comparator.comparing(
                DefendantAccountHistoryItem::getEventDateTime,
                Comparator.nullsLast(Comparator.reverseOrder())
            )
            .thenComparing(DefendantAccountHistoryItem::getType, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(DefendantAccountHistoryItem::getSourceId, Comparator.nullsLast(Comparator.naturalOrder()));
    }
}
