package uk.gov.hmcts.opal.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AccountEnquiryDtoTest {

    @Test
    public void testBuilder() {
        AccountEnquiryDto accountEnquiryDto = AccountEnquiryDto.builder()
            .businessUnitId((short) 1)
            .accountNumber("123456")
            .build();
        assertEquals((short)1, accountEnquiryDto.getBusinessUnitId());
        assertEquals("123456", accountEnquiryDto.getAccountNumber());
        assertNotNull(AccountEnquiryDto.builder().toString());
    }

    @Test
    public void testDataClass() {
        AccountEnquiryDto accountEnquiryDto = AccountEnquiryDto.builder().build();

        accountEnquiryDto.setAccountNumber("123456");
        accountEnquiryDto.setBusinessUnitId((short)1);

        assertEquals((short)1, accountEnquiryDto.getBusinessUnitId());
        assertEquals("123456", accountEnquiryDto.getAccountNumber());
    }

    @Test
    public void testToJsonString() throws Exception {
        AccountEnquiryDto accountEnquiryDto = AccountEnquiryDto.builder()
            .businessUnitId((short) 1)
            .accountNumber("123456")
            .build();

        assertNotNull(accountEnquiryDto.toJsonString());
    }

    @Test
    public void testControllerModelEqualsAndHashCode() {
        // Arrange
        AccountEnquiryDto model1 = AccountEnquiryDto.builder().build();
        AccountEnquiryDto model2 = AccountEnquiryDto.builder().build();

        // Assert
        assertEquals(model1, model2);
        assertEquals(model1.hashCode(), model2.hashCode());
    }

    @Test
    public void testControllerModelToString() {
        // Arrange
        AccountEnquiryDto model = AccountEnquiryDto.builder().build();

        // Act
        String result = model.toString();

        // Assert
        assertNotNull(result);
    }
}
