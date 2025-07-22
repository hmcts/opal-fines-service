package uk.gov.hmcts.opal.dto.legacy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j(topic = "opal.LegacyDefendantAccountSearchResultTest")
public class LegacyDefendantAccountSearchResultTest {

    @Test
    void testBuilder() {
        LegacyDefendantAccountSearchResult accountEnquiryDto = constructTestDefendantAccountSearchResult();

        assertEquals("accountNo", accountEnquiryDto.getAccountNumber());
        assertEquals("Mr John Smith", accountEnquiryDto.getFullName());
        assertEquals("Scotland", accountEnquiryDto.getAddressLine1());
        assertEquals(LocalDate.of(1977, 6, 26), accountEnquiryDto.getBirthDate());
        assertEquals(BigDecimal.valueOf(1000), accountEnquiryDto.getAccountBalance());
        assertEquals("9", accountEnquiryDto.getBusinessUnitId());

        assertNotNull(AccountEnquiryDto.builder().toString());
    }

    @Test
    void testJsonString() throws Exception {
        LegacyDefendantAccountSearchResult model = constructTestDefendantAccountSearchResult();
        assertNotNull(model.toJsonString());

        LegacyDefendantAccountsSearchResults parsed = ToJsonString.getObjectMapper()
            .readValue(getJsonRepresentation(), LegacyDefendantAccountsSearchResults.class);
        assertNotNull(parsed);
    }


    @Test
    void testControllerModelEqualsAndHashCode() {
        // Arrange
        LegacyDefendantAccountSearchResult model1 = LegacyDefendantAccountSearchResult.builder().build();
        LegacyDefendantAccountSearchResult model2 = LegacyDefendantAccountSearchResult.builder().build();

        // Assert
        assertEquals(model1, model2);
        assertEquals(model1.hashCode(), model2.hashCode());
    }

    @Test
    void testControllerModelToDefendantAccountSummaryDto() {
        // Arrange
        LegacyDefendantAccountSearchResult model1 = LegacyDefendantAccountSearchResult.builder().build();
        LegacyDefendantAccountSearchResult model2 = constructTestDefendantAccountSearchResult();

        // Assert
        assertNotNull(model1.toString());
        assertNotNull(model2.toString());

        assertNotNull(model1.toDefendantAccountSummaryDto());
        assertNotNull(model2.toDefendantAccountSummaryDto());

        DefendantAccountSummaryDto dto = model2.toDefendantAccountSummaryDto();
        assertEquals("Smith", dto.getDefendantSurname());
        assertEquals("John", dto.getDefendantFirstnames());
        assertEquals("Mr", dto.getDefendantTitle());
        assertEquals("accountNo", dto.getAccountNumber());
        assertEquals("12345", dto.getDefendantAccountId());
        assertEquals(1000.0, dto.getAccountBalance());
        assertEquals("Scotland", dto.getAddressLine1());
        assertEquals("1977-06-26", dto.getBirthDate());

    }

    static LegacyDefendantAccountSearchResult constructTestDefendantAccountSearchResult() {
        return LegacyDefendantAccountSearchResult.builder()
            .accountNumber("accountNo")
            .defendantAccountId(12345L)
            .surname("Smith")
            .forenames("John")
            .title("Mr")
            .birthDate(LocalDate.parse("1977-06-26"))
            .addressLine1("Scotland")
            .accountBalance(BigDecimal.valueOf(1000))
            .businessUnitId("9")
            .businessUnitName("Cardiff")
            .build();
    }

    private String getJsonRepresentation() {
        return """
        {
          "defendant_accounts" : [ {
            "account_number" : "accountNo",
            "organisation" : false,
            "defendant_title" : "Mr",
            "defendant_surname" : "Smith",
            "defendant_firstnames" : "John",
            "defendant_account_id" : 12345,
            "business_unit_id" : "9",
            "business_unit_name" : "Cardiff",
            "organisation_name" : null,
            "birth_date" : "1977-06-26",
            "address_line_1" : "Scotland",
            "account_balance" : 1000
          } ],
          "count" : 1
        }
            """;
    }
}
