package uk.gov.hmcts.opal.service.opal.history.defendant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
import uk.gov.hmcts.opal.dto.history.NoteDetails;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryFilter;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryItem;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryItemType;

class DefendantAccountHistoryModelAdapterTest {

    @Test
    void toCoreFilter_mapsDefendantFilterToCoreFilter() {
        DefendantAccountHistoryFilter filter = DefendantAccountHistoryFilter.builder()
            .dateFrom(LocalDate.of(2026, 1, 1))
            .dateTo(LocalDate.of(2026, 1, 31))
            .itemTypes(List.of(HistoryItemType.NOTE, HistoryItemType.PAYMENT_TERMS))
            .build();

        AccountHistoryFilter result = DefendantAccountHistoryModelAdapter.toCoreFilter(filter);

        assertEquals(LocalDate.of(2026, 1, 1), result.getDateFrom());
        assertEquals(LocalDate.of(2026, 1, 31), result.getDateTo());
        assertEquals(List.of(AccountHistoryItemType.NOTE, AccountHistoryItemType.PAYMENT_TERMS),
            result.getItemTypes());
    }

    @Test
    void toCoreFilter_preservesNullItemTypes() {
        AccountHistoryFilter result = DefendantAccountHistoryModelAdapter.toCoreFilter(
            DefendantAccountHistoryFilter.builder().build()
        );

        assertNull(result.getItemTypes());
    }

    @Test
    void mapsHistoryItemsBetweenDefendantAndCoreModels() {
        DefendantAccountHistoryItem defendantItem = DefendantAccountHistoryItem.builder()
            .postedDetails(PostedDetails.builder()
                .postedDate(LocalDateTime.of(2026, 1, 4, 9, 0))
                .postedBy("hist-user")
                .postedByName("History User")
                .build())
            .type(HistoryItemType.NOTE)
            .details(NoteDetails.builder().noteText("History note").build())
            .amount(new BigDecimal("10.50"))
            .eventDateTime(LocalDateTime.of(2026, 1, 4, 9, 0))
            .sourceId(44L)
            .build();

        AccountHistoryItem coreItem = DefendantAccountHistoryModelAdapter.toCoreItem(defendantItem);

        assertEquals(AccountHistoryItemType.NOTE, coreItem.getType());
        assertEquals(defendantItem.getPostedDetails(), coreItem.getPostedDetails());
        assertEquals(defendantItem.getDetails(), coreItem.getDetails());
        assertEquals(defendantItem.getAmount(), coreItem.getAmount());
        assertEquals(defendantItem.getEventDateTime(), coreItem.getEventDateTime());
        assertEquals(defendantItem.getSourceId(), coreItem.getSourceId());

        DefendantAccountHistoryItem mappedBack = DefendantAccountHistoryModelAdapter.toDefendantItem(coreItem);
        assertEquals(defendantItem.getType(), mappedBack.getType());
        assertEquals(defendantItem.getPostedDetails(), mappedBack.getPostedDetails());
        assertEquals(defendantItem.getDetails(), mappedBack.getDetails());
        assertEquals(defendantItem.getAmount(), mappedBack.getAmount());
        assertEquals(defendantItem.getEventDateTime(), mappedBack.getEventDateTime());
        assertEquals(defendantItem.getSourceId(), mappedBack.getSourceId());
    }

    @Test
    void mapsItemTypesBetweenDefendantAndCoreEnums() {
        assertEquals(AccountHistoryItemType.AMENDMENT,
            DefendantAccountHistoryModelAdapter.toCoreItemType(HistoryItemType.AMENDMENT));
        assertEquals(HistoryItemType.FINANCIAL,
            DefendantAccountHistoryModelAdapter.toDefendantItemType(AccountHistoryItemType.FINANCIAL));
    }
}
