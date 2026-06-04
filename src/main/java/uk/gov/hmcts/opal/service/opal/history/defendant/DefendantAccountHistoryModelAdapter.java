package uk.gov.hmcts.opal.service.opal.history.defendant;

import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryFilter;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryItem;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryItemType;

public final class DefendantAccountHistoryModelAdapter {

    private DefendantAccountHistoryModelAdapter() {
    }

    public static AccountHistoryFilter toCoreFilter(DefendantAccountHistoryFilter filter) {
        return AccountHistoryFilter.builder()
            .dateFrom(filter.getDateFrom())
            .dateTo(filter.getDateTo())
            .itemTypes(filter.getItemTypes() == null ? null : filter.getItemTypes().stream()
                .map(DefendantAccountHistoryModelAdapter::toCoreItemType)
                .toList())
            .build();
    }

    public static AccountHistoryItem toCoreItem(DefendantAccountHistoryItem item) {
        return AccountHistoryItem.builder()
            .postedDetails(item.getPostedDetails())
            .type(toCoreItemType(item.getType()))
            .details(item.getDetails())
            .amount(item.getAmount())
            .eventDateTime(item.getEventDateTime())
            .sourceId(item.getSourceId())
            .build();
    }

    public static DefendantAccountHistoryItem toDefendantItem(AccountHistoryItem item) {
        return DefendantAccountHistoryItem.builder()
            .postedDetails(item.getPostedDetails())
            .type(toDefendantItemType(item.getType()))
            .details(item.getDetails())
            .amount(item.getAmount())
            .eventDateTime(item.getEventDateTime())
            .sourceId(item.getSourceId())
            .build();
    }

    public static AccountHistoryItemType toCoreItemType(HistoryItemType type) {
        return switch (type) {
            case AMENDMENT -> AccountHistoryItemType.AMENDMENT;
            case ENFORCEMENT -> AccountHistoryItemType.ENFORCEMENT;
            case FINANCIAL -> AccountHistoryItemType.FINANCIAL;
            case NOTE -> AccountHistoryItemType.NOTE;
            case PAYMENT_TERMS -> AccountHistoryItemType.PAYMENT_TERMS;
        };
    }

    public static HistoryItemType toDefendantItemType(AccountHistoryItemType type) {
        return switch (type) {
            case AMENDMENT -> HistoryItemType.AMENDMENT;
            case ENFORCEMENT -> HistoryItemType.ENFORCEMENT;
            case FINANCIAL -> HistoryItemType.FINANCIAL;
            case NOTE -> HistoryItemType.NOTE;
            case PAYMENT_TERMS -> HistoryItemType.PAYMENT_TERMS;
        };
    }
}
