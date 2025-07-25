package uk.gov.hmcts.opal.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DefendantAccountSummaryDtoTest {

    @Test
    void testBuilder() {
        final LocalDate today = LocalDate.now();
        DefendantAccountSummaryDto accountEnquiryDto = constructTestAccountSummaryDto(today);

        assertEquals("accountNameNo", accountEnquiryDto.getAccountNumber());
        assertEquals("Mr", accountEnquiryDto.getDefendantTitle());
        assertEquals("John", accountEnquiryDto.getDefendantFirstnames());
        assertEquals("Smith", accountEnquiryDto.getDefendantSurname());
        assertEquals("Scotland", accountEnquiryDto.getAddressLine1());
        assertEquals(today.toString(), accountEnquiryDto.getBirthDate());
        assertEquals(1000.0, accountEnquiryDto.getAccountBalance());

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
            .accountNumber("accountNameNo")
            .defendantTitle("Mr")
            .defendantFirstnames("John")
            .defendantSurname("Smith")
            .birthDate(today.toString())
            .addressLine1("Scotland")
            .accountBalance(1000.0)
            .build();
    }
}
