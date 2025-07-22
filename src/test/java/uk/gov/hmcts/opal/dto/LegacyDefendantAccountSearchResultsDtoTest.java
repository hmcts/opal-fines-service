package uk.gov.hmcts.opal.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;

public class LegacyDefendantAccountSearchResultsDtoTest {

    @Test
    void testBuilder() {
        DefendantAccountSearchResultsDto accountEnquiryDto = constructAccountSearchResultsDto();
        assertEquals(999, accountEnquiryDto.getTotalCount());
        assertEquals(7, accountEnquiryDto.getCursor());
        assertEquals(100, accountEnquiryDto.getPageSize());
        assertEquals(1, accountEnquiryDto.getCount());
        assertNotNull(AccountEnquiryDto.builder().toString());
    }

    @Test
    void testToJsonString() throws Exception {
        DefendantAccountSearchResultsDto accountEnquiryDto = constructAccountSearchResultsDto();

        assertNotNull(accountEnquiryDto.toJsonString());
    }

    @Test
    void testControllerModelEqualsAndHashCode() {
        // Arrange
        DefendantAccountSearchResultsDto model1 = DefendantAccountSearchResultsDto.builder().build();
        DefendantAccountSearchResultsDto model2 = DefendantAccountSearchResultsDto.builder().build();

        // Assert
        assertEquals(model1, model2);
        assertEquals(model1.hashCode(), model2.hashCode());
    }

    @Test
    void testControllerModelToString() {
        // Arrange
        DefendantAccountSearchResultsDto model = DefendantAccountSearchResultsDto.builder().build();

        // Act
        String result = model.toString();

        // Assert
        assertNotNull(result);
    }

    private DefendantAccountSearchResultsDto constructAccountSearchResultsDto() {
        return DefendantAccountSearchResultsDto.builder()
            .searchResults(List.of(DefendantAccountSummaryDto.builder().build()))
            .totalCount(999L)
            .cursor(7)
            .build();
    }
}
