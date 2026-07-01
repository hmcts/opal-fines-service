package uk.gov.hmcts.opal.entity.minorcreditor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryItemType.AMENDMENT;
import static uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryItemType.FINANCIAL;
import static uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryItemType.NOTE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class MinorCreditorHistoryFiltersTest {

    @Test
    void from_withoutItemTypes_includesAllTypes() {
        MinorCreditorHistoryFilters filters = MinorCreditorHistoryFilters.from(null, null, null);

        assertThat(filters.itemTypes()).containsExactlyInAnyOrder(AMENDMENT, FINANCIAL, NOTE);
        assertThat(filters.includes(AMENDMENT)).isTrue();
        assertThat(filters.includes(FINANCIAL)).isTrue();
        assertThat(filters.includes(NOTE)).isTrue();
    }

    @Test
    void from_withDateRange_setsInclusiveStartAndExclusiveEnd() {
        MinorCreditorHistoryFilters filters = MinorCreditorHistoryFilters.from(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 31),
            null);

        assertThat(filters.postedFromInclusive()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
        assertThat(filters.postedToExclusive()).isEqualTo(LocalDateTime.of(2026, 2, 1, 0, 0));
    }

    @Test
    void from_withRepeatedAndCommaSeparatedItemTypes_parsesAllValues() {
        MinorCreditorHistoryFilters filters = MinorCreditorHistoryFilters.from(
            null,
            null,
            List.of("amendment,note", "financial"));

        assertThat(filters.itemTypes()).containsExactlyInAnyOrder(AMENDMENT, FINANCIAL, NOTE);
    }

    @Test
    void from_withInvalidItemType_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> MinorCreditorHistoryFilters.from(null, null, List.of("payment")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("itemTypes must contain only amendment, financial, note");
    }

    @Test
    void from_withBlankItemType_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> MinorCreditorHistoryFilters.from(null, null, List.of("")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("itemTypes must contain only amendment, financial, note");
    }

    @Test
    void from_withDateFromAfterDateTo_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> MinorCreditorHistoryFilters.from(
            LocalDate.of(2026, 2, 1),
            LocalDate.of(2026, 1, 31),
            null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("dateFrom must be on or before dateTo");
    }
}
