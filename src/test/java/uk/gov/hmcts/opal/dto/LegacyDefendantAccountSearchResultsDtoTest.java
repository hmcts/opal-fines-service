package uk.gov.hmcts.opal.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;

public class LegacyDefendantAccountSearchResultsDtoTest {

    @Test
    void testBuilder() {
        DefendantAccountSearchResultsDto accountEnquiryDto = constructAccountSearchResultsDto();
        assertEquals(1, accountEnquiryDto.getCount());
        assertEquals(1, accountEnquiryDto.getDefendantAccounts().size());
        assertNotNull(AccountEnquiryDto.builder().toString());
    }

    @Test
    void testToJsonString() throws Exception {
        DefendantAccountSearchResultsDto accountEnquiryDto = constructAccountSearchResultsDto();

        assertNotNull(accountEnquiryDto.toJsonString());
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
        List<DefendantAccountSummaryDto> accounts = List.of(
            DefendantAccountSummaryDto.builder()
                .defendantAccountId("123")
                .accountNumber("ACC-001")
                .defendantFirstnames("Jane")
                .defendantSurname("Doe")
                .birthDate("1980-01-01")
                .addressLine1("123 Street")
                .accountBalance(new BigDecimal(100.0))
                .organisation(false)
                .build()
        );

        return DefendantAccountSearchResultsDto.builder()
            .defendantAccounts(accounts)
            .build();
    }

}
