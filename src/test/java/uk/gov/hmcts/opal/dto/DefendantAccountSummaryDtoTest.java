package uk.gov.hmcts.opal.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class DefendantAccountSummaryDtoTest {

    @Test
    void testBuilder() {
        final LocalDate today = LocalDate.now();
        DefendantAccountSummaryDto accountEnquiryDto = constructTestAccountSummaryDto(today);

        assertEquals("accountNameNo", accountEnquiryDto.getAccountNo());
        assertEquals("Mr John Smith", accountEnquiryDto.getName());
        assertEquals("Scotland", accountEnquiryDto.getAddressLine1());
        assertEquals(today, accountEnquiryDto.getDateOfBirth());
        assertEquals(BigDecimal.valueOf(1000), accountEnquiryDto.getBalance());
        assertEquals("London", accountEnquiryDto.getCourt());

        assertNotNull(AccountEnquiryDto.builder().toString());
    }

    @Test
    void testToJsonString() throws Exception {
        DefendantAccountSummaryDto accountEnquiryDto = constructTestAccountSummaryDto(LocalDate.now());

        assertNotNull(accountEnquiryDto.toJsonString());
    }

    @Test
    void testControllerModelEqualsAndHashCode() {
        // Arrange
        DefendantAccountSummaryDto model1 = DefendantAccountSummaryDto.builder().build();
        DefendantAccountSummaryDto model2 = DefendantAccountSummaryDto.builder().build();

        // Assert
        assertEquals(model1, model2);
        assertEquals(model1.hashCode(), model2.hashCode());
    }

    @Test
    void testControllerModelToString() {
        // Arrange
        DefendantAccountSummaryDto model = DefendantAccountSummaryDto.builder().build();

        // Act
        String result = model.toString();

        // Assert
        assertNotNull(result);
    }

    private DefendantAccountSummaryDto constructTestAccountSummaryDto(final LocalDate today) {
        return DefendantAccountSummaryDto.builder()
            .accountNo("accountNameNo")
            .name("Mr John Smith")
            .dateOfBirth(today)
            .addressLine1("Scotland")
            .balance(BigDecimal.valueOf(1000))
            .court("London")
            .build();
    }
}
