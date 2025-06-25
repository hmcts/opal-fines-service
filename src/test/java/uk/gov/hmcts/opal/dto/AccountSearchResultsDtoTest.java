package uk.gov.hmcts.opal.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.search.AccountSearchResultsDto;

public class AccountSearchResultsDtoTest {

    @Test
    void testBuilder() {
        AccountSearchResultsDto accountEnquiryDto = constructAccountSearchResultsDto();
        assertEquals(999, accountEnquiryDto.getTotalCount());
        assertEquals(7, accountEnquiryDto.getCursor());
        assertEquals(100, accountEnquiryDto.getPageSize());
        assertEquals(1, accountEnquiryDto.getCount());
        assertNotNull(AccountEnquiryDto.builder().toString());
    }

    @Test
    void testToJsonString() throws Exception {
        AccountSearchResultsDto accountEnquiryDto = constructAccountSearchResultsDto();

        assertNotNull(accountEnquiryDto.toJsonString());
    }

    @Test
    void testControllerModelEqualsAndHashCode() {
        // Arrange
        AccountSearchResultsDto model1 = AccountSearchResultsDto.builder().build();
        AccountSearchResultsDto model2 = AccountSearchResultsDto.builder().build();

        // Assert
        assertEquals(model1, model2);
        assertEquals(model1.hashCode(), model2.hashCode());
    }

    @Test
    void testControllerModelToString() {
        // Arrange
        AccountSearchResultsDto model = AccountSearchResultsDto.builder().build();

        // Act
        String result = model.toString();

        // Assert
        assertNotNull(result);
    }

    private AccountSearchResultsDto constructAccountSearchResultsDto() {
        return AccountSearchResultsDto.builder()
            .searchResults(List.of(AccountSummaryDto.builder().build()))
            .totalCount(999L)
            .cursor(7)
            .build();
    }
}
