package uk.gov.hmcts.opal.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.opal.dto.DateDto.fromLocalDate;

import java.time.LocalDate;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class AccountSearchDtoTest {

    @Test
    public void testBuilder() {
        final LocalDate today = LocalDate.now();
        AccountSearchDto accountEnquiryDto = constructTestAccountSearchDto(today);
        assertEquals("Bath", accountEnquiryDto.getCourt());
        assertEquals("Smith", accountEnquiryDto.getSurname());
        assertEquals("Scotland", accountEnquiryDto.getAddressLineOne());
        assertEquals("case001", accountEnquiryDto.getPcr());
        assertEquals(fromLocalDate(today), accountEnquiryDto.getDateOfBirth());
        assertEquals("Dave", accountEnquiryDto.getForename());
        assertEquals("HRH", accountEnquiryDto.getInitials());
        assertEquals("QUICK", accountEnquiryDto.getSearchType());
        assertEquals("VISA", accountEnquiryDto.getMajorCreditor());
        assertEquals("XX12345678", accountEnquiryDto.getNiNumber());
        assertEquals("6", accountEnquiryDto.getTillNumber());

        assertNotNull(AccountEnquiryDto.builder().toString());
    }

    @Test
    public void testToJsonString() throws Exception {
        AccountSearchDto accountEnquiryDto = constructTestAccountSearchDto(LocalDate.now());
        assertNotNull(accountEnquiryDto.toJsonString());
    }

    @Test
    public void testControllerModelEqualsAndHashCode() {
        // Arrange
        AccountSearchDto model1 = AccountSearchDto.builder().build();
        AccountSearchDto model2 = AccountSearchDto.builder().build();

        // Assert
        assertEquals(model1, model2);
        assertEquals(model1.hashCode(), model2.hashCode());
    }

    @Test
    public void testControllerModelToString() {
        // Arrange
        AccountSearchDto model = AccountSearchDto.builder().build();

        // Act
        String result = model.toString();

        // Assert
        assertNotNull(result);
    }

    private AccountSearchDto constructTestAccountSearchDto(final LocalDate today) {
        return AccountSearchDto.builder()
            .court("Bath")
            .surname("Smith")
            .addressLineOne("Scotland")
            .pcr("case001")
            .dateOfBirth(fromLocalDate(today))
            .forename("Dave")
            .initials("HRH")
            .searchType("QUICK")
            .majorCreditor("VISA")
            .niNumber("XX12345678")
            .tillNumber("6")
            .build();
    }
}
