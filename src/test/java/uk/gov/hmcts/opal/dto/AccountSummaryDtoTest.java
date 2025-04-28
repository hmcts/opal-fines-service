package uk.gov.hmcts.opal.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AccountSummaryDtoTest {

    @Test
    public void testBuilder() {
        final LocalDate today = LocalDate.now();
        AccountSummaryDto accountEnquiryDto = constructTestAccountSummaryDto(today);

        assertEquals("accountNameNo", accountEnquiryDto.getAccountNo());
        assertEquals("Mr John Smith", accountEnquiryDto.getName());
        assertEquals("Scotland", accountEnquiryDto.getAddressLine1());
        assertEquals(today, accountEnquiryDto.getDateOfBirth());
        assertEquals(BigDecimal.valueOf(1000), accountEnquiryDto.getBalance());
        assertEquals("London", accountEnquiryDto.getCourt());

        assertNotNull(AccountEnquiryDto.builder().toString());
    }

    @Test
    public void testToJsonString() throws Exception {
        AccountSummaryDto accountEnquiryDto = constructTestAccountSummaryDto(LocalDate.now());

        assertNotNull(accountEnquiryDto.toJsonString());
    }

    @Test
    public void testControllerModelEqualsAndHashCode() {
        // Arrange
        AccountSummaryDto model1 = AccountSummaryDto.builder().build();
        AccountSummaryDto model2 = AccountSummaryDto.builder().build();

        // Assert
        assertEquals(model1, model2);
        assertEquals(model1.hashCode(), model2.hashCode());
    }

    @Test
    public void testControllerModelToString() {
        // Arrange
        AccountSummaryDto model = AccountSummaryDto.builder().build();

        // Act
        String result = model.toString();

        // Assert
        assertNotNull(result);
    }

    private AccountSummaryDto constructTestAccountSummaryDto(final LocalDate today) {
        return AccountSummaryDto.builder()
            .accountNo("accountNameNo")
            .name("Mr John Smith")
            .dateOfBirth(today)
            .addressLine1("Scotland")
            .balance(BigDecimal.valueOf(1000))
            .court("London")
            .build();
    }
}
