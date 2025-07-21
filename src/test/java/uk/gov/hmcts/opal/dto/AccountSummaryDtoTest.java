package uk.gov.hmcts.opal.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class AccountSummaryDtoTest {

    @Test
    void testBuilder() {
        final LocalDate today = LocalDate.now();
        AccountSummaryDto accountEnquiryDto = constructTestAccountSummaryDto(today);

        assertEquals("accountNameNo", accountEnquiryDto.getAccountNumber());
        assertEquals("Mr", accountEnquiryDto.getDefendantTitle());
        assertEquals("John", accountEnquiryDto.getDefendantFirstnames());
        assertEquals("Smith", accountEnquiryDto.getDefendantSurname());
        assertEquals("Scotland", accountEnquiryDto.getAddressLine1());
        assertEquals(today, accountEnquiryDto.getBirthDate());
        assertEquals(BigDecimal.valueOf(1000), accountEnquiryDto.getAccountBalance());
        assertEquals("London", accountEnquiryDto.getBusinessUnitName());

        assertNotNull(AccountEnquiryDto.builder().toString());
    }

    @Test
    void testToJsonString() throws Exception {
        AccountSummaryDto accountEnquiryDto = constructTestAccountSummaryDto(LocalDate.now());

        assertNotNull(accountEnquiryDto.toJsonString());
    }

    @Test
    void testControllerModelEqualsAndHashCode() {
        // Arrange
        AccountSummaryDto model1 = AccountSummaryDto.builder().build();
        AccountSummaryDto model2 = AccountSummaryDto.builder().build();

        // Assert
        assertEquals(model1, model2);
        assertEquals(model1.hashCode(), model2.hashCode());
    }

    @Test
    void testControllerModelToString() {
        // Arrange
        AccountSummaryDto model = AccountSummaryDto.builder().build();

        // Act
        String result = model.toString();

        // Assert
        assertNotNull(result);
    }

    private AccountSummaryDto constructTestAccountSummaryDto(final LocalDate today) {
        return AccountSummaryDto.builder()
            .accountNumber("accountNameNo")
            .defendantTitle("Mr")
            .defendantFirstnames("John")
            .defendantSurname("Smith")
            .birthDate(today)
            .addressLine1("Scotland")
            .accountBalance(BigDecimal.valueOf(1000))
            .businessUnitName("London")
            .build();
    }
}
