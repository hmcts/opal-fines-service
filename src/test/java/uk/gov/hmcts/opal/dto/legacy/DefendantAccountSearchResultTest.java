package uk.gov.hmcts.opal.dto.legacy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.AccountSummaryDto;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j(topic = "opal.DefendantAccountSearchResultTest")
public class DefendantAccountSearchResultTest {

    @Test
    public void testBuilder() {
        DefendantAccountSearchResult accountEnquiryDto = constructTestDefendantAccountSearchResult();

        assertEquals("accountNo", accountEnquiryDto.getAccountNumber());
        assertEquals("Mr John Smith", accountEnquiryDto.getFullName());
        assertEquals("Scotland", accountEnquiryDto.getAddressLine1());
        assertEquals("1977-06-26", accountEnquiryDto.getBirthDate());
        assertEquals(BigDecimal.valueOf(1000), accountEnquiryDto.getAccountBalance());
        assertEquals(9, accountEnquiryDto.getBusinessUnitId());

        assertNotNull(AccountEnquiryDto.builder().toString());
    }

    @Test
    public void testJsonString() throws Exception {
        DefendantAccountSearchResult model = constructTestDefendantAccountSearchResult();
        assertNotNull(model.toJsonString());

        DefendantAccountsSearchResults parsed = ToJsonString.getObjectMapper()
            .readValue(getJsonRepresentation(), DefendantAccountsSearchResults.class);
        assertNotNull(parsed);
    }


    @Test
    public void testControllerModelEqualsAndHashCode() {
        // Arrange
        DefendantAccountSearchResult model1 = DefendantAccountSearchResult.builder().build();
        DefendantAccountSearchResult model2 = DefendantAccountSearchResult.builder().build();

        // Assert
        assertEquals(model1, model2);
        assertEquals(model1.hashCode(), model2.hashCode());
    }

    @Test
    public void testControllerModelToAccountSummaryDto() {
        // Arrange
        DefendantAccountSearchResult model1 = DefendantAccountSearchResult.builder().build();
        DefendantAccountSearchResult model2 = constructTestDefendantAccountSearchResult();

        // Assert
        assertNotNull(model1.toString());
        assertNotNull(model2.toString());

        assertNotNull(model1.toAccountSummaryDto());
        assertNotNull(model2.toAccountSummaryDto());

        AccountSummaryDto dto = model2.toAccountSummaryDto();
        assertEquals("Mr John Smith", dto.getName());
        assertEquals("accountNo", dto.getAccountNo());
        assertEquals("Cardiff", dto.getCourt());
        assertEquals(12345L, dto.getDefendantAccountId());
        assertEquals(BigDecimal.valueOf(1000), dto.getBalance());
        assertEquals("Scotland", dto.getAddressLine1());
        assertEquals(LocalDate.of(1977, 6, 26), dto.getDateOfBirth());

    }

    public static DefendantAccountSearchResult constructTestDefendantAccountSearchResult() {
        return DefendantAccountSearchResult.builder()
            .accountNumber("accountNo")
            .defendantAccountId(12345L)
            .surname("Smith")
            .forenames("John")
            .title("Mr")
            .birthDate("1977-06-26")
            .addressLine1("Scotland")
            .accountBalance(BigDecimal.valueOf(1000))
            .businessUnitId(9)
            .businessUnitName("Cardiff")
            .build();
    }

    private String getJsonRepresentation() {
        return """
            {
              "defendantAccountsSearchResult" : [ {
                "accountNumber" : "accountNo",
                "organisation" : null,
                "title" : "Mr",
                "surname" : "Smith",
                "forenames" : "John",
                "initials" : null,
                "rowNumber" : null,
                "defendant_account_id" : 12345,
                "business_unit_id" : 9,
                "business_unit_name" : "Cardiff",
                "organisation_name" : null,
                "birth_date" : "1977-06-26",
                "address_line_1" : "Scotland",
                "account_balance" : 1000
              } ],
              "totalCount" : 1
            }
            """;
    }
}
