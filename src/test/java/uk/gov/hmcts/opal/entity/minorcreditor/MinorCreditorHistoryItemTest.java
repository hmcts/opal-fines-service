package uk.gov.hmcts.opal.entity.minorcreditor;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryItemType.AMENDMENT;
import static uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryItemType.FINANCIAL;
import static uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryItemType.NOTE;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.generated.model.MinorCreditorHistoryItemHistory;

class MinorCreditorHistoryItemTest {

    @Test
    void ordering_sortsByNewestTimestampThenTypeOrderThenSourceId() {
        // Arrange
        LocalDateTime olderTimestamp = LocalDateTime.of(2026, 1, 30, 10, 0);
        LocalDateTime newerTimestamp = LocalDateTime.of(2026, 1, 31, 10, 0);
        MinorCreditorHistoryItem olderAmendment = item(AMENDMENT, 1L, olderTimestamp);
        MinorCreditorHistoryItem note = item(NOTE, 1L, newerTimestamp);
        MinorCreditorHistoryItem financial = item(FINANCIAL, 1L, newerTimestamp);
        MinorCreditorHistoryItem amendmentHighId = item(AMENDMENT, 3L, newerTimestamp);
        MinorCreditorHistoryItem amendmentLowId = item(AMENDMENT, 2L, newerTimestamp);

        // Act
        List<MinorCreditorHistoryItem> sorted = List.of(
            olderAmendment,
            note,
            financial,
            amendmentHighId,
            amendmentLowId
        ).stream().sorted(MinorCreditorHistoryItem.ORDERING).toList();

        // Assert
        assertThat(sorted).containsExactly(amendmentLowId, amendmentHighId, financial, note, olderAmendment);
    }

    private MinorCreditorHistoryItem item(
        MinorCreditorHistoryItemType sourceType,
        Long sourceId,
        LocalDateTime postedDate) {
        return new MinorCreditorHistoryItem(sourceType, sourceId, postedDate, new MinorCreditorHistoryItemHistory());
    }
}
