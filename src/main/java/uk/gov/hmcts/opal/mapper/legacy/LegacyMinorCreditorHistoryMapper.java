package uk.gov.hmcts.opal.mapper.legacy;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryRequest;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryFilters;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryItemType;

@Component
public class LegacyMinorCreditorHistoryMapper {

    public LegacyGetMinorCreditorAccountHistoryRequest toLegacyRequest(
        Long minorCreditorAccountId,
        MinorCreditorHistoryFilters filters) {

        return LegacyGetMinorCreditorAccountHistoryRequest.builder()
            .creditorAccountId(String.valueOf(minorCreditorAccountId))
            .fromDate(filters == null ? null : filters.dateFrom())
            .toDate(filters == null ? null : filters.dateTo())
            .itemTypes(toLegacyItemTypes(filters))
            .build();
    }

    private List<String> toLegacyItemTypes(MinorCreditorHistoryFilters filters) {
        if (filters == null || filters.itemTypes().size() == MinorCreditorHistoryItemType.values().length) {
            return null;
        }

        return Arrays.stream(MinorCreditorHistoryItemType.values())
            .filter(filters::includes)
            .map(this::toLegacyItemType)
            .toList();
    }

    private String toLegacyItemType(MinorCreditorHistoryItemType itemType) {
        return switch (itemType) {
            case AMENDMENT -> "Amendment";
            case FINANCIAL -> "Financial";
            case NOTE -> "Note";
        };
    }
}
