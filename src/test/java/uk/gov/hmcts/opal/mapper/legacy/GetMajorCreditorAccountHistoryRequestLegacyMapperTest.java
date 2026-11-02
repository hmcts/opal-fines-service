package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyRequest;
import uk.gov.hmcts.opal.mapper.AbstractMapperTest;

class GetMajorCreditorAccountHistoryRequestLegacyMapperTest extends AbstractMapperTest {

    @Autowired
    private GetMajorCreditorAccountHistoryRequestLegacyMapper mapper;

    @Test
    void toLegacyRequest_mapsRequestValuesAndProvidesAllSupportedItemTypes() {
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 1, 31);

        GetMajorCreditorAccountHistoryLegacyRequest result =
            mapper.toLegacyRequest(123L, dateFrom, dateTo, List.of("Financial", "Note"));

        assertEquals("123", result.getCreditorAccountId());
        assertEquals(dateFrom, result.getFromDate());
        assertEquals(dateTo, result.getToDate());
        assertEquals(List.of("Financial", "Note"), result.getItemTypes());
    }

    @Test
    void toLegacyRequest_mapsRequestValuesAndProvidedNoteItemType() {
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 1, 31);

        GetMajorCreditorAccountHistoryLegacyRequest result =
            mapper.toLegacyRequest(123L, dateFrom, dateTo, List.of("Note"));

        assertEquals("123", result.getCreditorAccountId());
        assertEquals(dateFrom, result.getFromDate());
        assertEquals(dateTo, result.getToDate());
        assertEquals(List.of("Note"), result.getItemTypes());
    }

    @Test
    void toLegacyRequest_mapsRequestValuesAndProvidedFinancialItemType() {
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 1, 31);

        GetMajorCreditorAccountHistoryLegacyRequest result =
            mapper.toLegacyRequest(123L, dateFrom, dateTo, List.of("Financial"));

        assertEquals("123", result.getCreditorAccountId());
        assertEquals(dateFrom, result.getFromDate());
        assertEquals(dateTo, result.getToDate());
        assertEquals(List.of("Financial"), result.getItemTypes());
    }

    @Test
    void toLegacyRequest_mapsRequestValuesAndEmptyItemTypes() {
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 1, 31);

        GetMajorCreditorAccountHistoryLegacyRequest result =
            mapper.toLegacyRequest(123L, dateFrom, dateTo, List.of());

        assertEquals("123", result.getCreditorAccountId());
        assertEquals(dateFrom, result.getFromDate());
        assertEquals(dateTo, result.getToDate());
        assertEquals(List.of("Financial", "Note"), result.getItemTypes());
    }
}
