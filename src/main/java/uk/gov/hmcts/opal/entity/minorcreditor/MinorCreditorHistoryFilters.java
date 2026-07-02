package uk.gov.hmcts.opal.entity.minorcreditor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public record MinorCreditorHistoryFilters(
    LocalDate dateFrom,
    LocalDate dateTo,
    LocalDateTime postedFromInclusive,
    LocalDateTime postedToExclusive,
    Set<MinorCreditorHistoryItemType> itemTypes) {

    public MinorCreditorHistoryFilters {
        itemTypes = Set.copyOf(itemTypes);
    }

    public static MinorCreditorHistoryFilters from(
        LocalDate dateFrom,
        LocalDate dateTo,
        List<String> itemTypes) {

        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom must be on or before dateTo");
        }

        return new MinorCreditorHistoryFilters(
            dateFrom,
            dateTo,
            dateFrom == null ? null : dateFrom.atStartOfDay(),
            dateTo == null ? null : dateTo.plusDays(1).atStartOfDay(),
            parseItemTypes(itemTypes));
    }

    public boolean includes(MinorCreditorHistoryItemType itemType) {
        return itemTypes.contains(itemType);
    }

    private static Set<MinorCreditorHistoryItemType> parseItemTypes(List<String> itemTypes) {
        List<String> queryValues = queryValues(itemTypes);
        if (queryValues.isEmpty()) {
            return EnumSet.allOf(MinorCreditorHistoryItemType.class);
        }

        EnumSet<MinorCreditorHistoryItemType> parsedItemTypes =
            EnumSet.noneOf(MinorCreditorHistoryItemType.class);
        queryValues.stream()
            .map(MinorCreditorHistoryItemType::fromQueryValue)
            .forEach(parsedItemTypes::add);
        return parsedItemTypes;
    }

    private static List<String> queryValues(List<String> itemTypes) {
        if (itemTypes == null) {
            return List.of();
        }

        return itemTypes.stream()
            .flatMap(rawValue -> rawValue == null ? Stream.of("") : Arrays.stream(rawValue.split(",", -1)))
            .map(String::trim)
            .toList();
    }
}
