package uk.gov.hmcts.opal.service.opal.history.defendant;

import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryFilter;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryItem;

public final class DefendantAccountHistoryModelAdapter {

    private DefendantAccountHistoryModelAdapter() {
    }

    public static AccountHistoryFilter toCoreFilter(DefendantAccountHistoryFilter filter) {
        return AccountHistoryFilter.builder()
            .dateFrom(filter.getDateFrom())
            .dateTo(filter.getDateTo())
            .itemTypes(filter.getItemTypes())
            .build();
    }

    public static AccountHistoryItem toCoreItem(DefendantAccountHistoryItem item) {
        return AccountHistoryItem.builder()
            .postedDetails(item.getPostedDetails())
            .type(item.getType())
            .details(item.getDetails())
            .amount(item.getAmount())
            .eventDateTime(item.getEventDateTime())
            .sourceId(item.getSourceId())
            .build();
    }

    public static DefendantAccountHistoryItem toDefendantItem(AccountHistoryItem item) {
        return DefendantAccountHistoryItem.builder()
            .postedDetails(item.getPostedDetails())
            .type(item.getType())
            .details(item.getDetails())
            .amount(item.getAmount())
            .eventDateTime(item.getEventDateTime())
            .sourceId(item.getSourceId())
            .build();
    }
}
