package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryRequest;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryFilters;

class MinorCreditorHistoryLegacyMapperTest {

    private final LegacyMinorCreditorHistoryMapper mapper = new LegacyMinorCreditorHistoryMapper();

    @Test
    void toLegacyRequest_whenNoFilters_mapsAccountIdAndOmitsItemTypes() {
        // Arrange
        MinorCreditorHistoryFilters filters = MinorCreditorHistoryFilters.from(null, null, null);

        // Act
        LegacyGetMinorCreditorAccountHistoryRequest result = mapper.toLegacyRequest(101L, filters);

        // Assert
        assertEquals("101", result.getCreditorAccountId());
        assertNull(result.getFromDate());
        assertNull(result.getToDate());
        assertNull(result.getItemTypes());
    }

    @Test
    void toLegacyRequest_whenDateRangeAndSingleType_mapsFilters() {
        // Arrange
        LocalDate dateFrom = LocalDate.of(2026, 1, 10);
        LocalDate dateTo = LocalDate.of(2026, 1, 31);
        MinorCreditorHistoryFilters filters = MinorCreditorHistoryFilters.from(dateFrom, dateTo, List.of("note"));

        // Act
        LegacyGetMinorCreditorAccountHistoryRequest result = mapper.toLegacyRequest(202L, filters);

        // Assert
        assertEquals("202", result.getCreditorAccountId());
        assertEquals(dateFrom, result.getFromDate());
        assertEquals(dateTo, result.getToDate());
        assertEquals(List.of("Note"), result.getItemTypes());
    }

    @Test
    void toLegacyRequest_whenAllTypesSelected_omitsItemTypes() {
        // Arrange
        MinorCreditorHistoryFilters filters = MinorCreditorHistoryFilters.from(
            null,
            null,
            List.of("note,financial,amendment")
        );

        // Act
        LegacyGetMinorCreditorAccountHistoryRequest result = mapper.toLegacyRequest(303L, filters);

        // Assert
        assertNull(result.getItemTypes());
    }

    @Test
    void toLegacyRequest_whenSomeTypes_mapsTypesInStableOrder() {
        // Arrange
        MinorCreditorHistoryFilters filters = MinorCreditorHistoryFilters.from(
            null,
            null,
            List.of("note,financial")
        );

        // Act
        LegacyGetMinorCreditorAccountHistoryRequest result = mapper.toLegacyRequest(404L, filters);

        // Assert
        assertEquals(List.of("Financial", "Note"), result.getItemTypes());
    }
}
