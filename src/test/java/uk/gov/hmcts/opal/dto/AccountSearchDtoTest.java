package uk.gov.hmcts.opal.dto;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantDto;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.opal.dto.DateDto.fromLocalDate;

@Slf4j(topic = "opal.AccountSearchDtoTest")
public class AccountSearchDtoTest {

    @Test
    void testBuilder() {
        final LocalDate today = LocalDate.now();
        AccountSearchDto accountEnquiryDto = constructTestAccountSearchDto(today);
        assertEquals("Bath", accountEnquiryDto.getCourt());
        assertEquals("Smith", accountEnquiryDto.getDefendant().getSurname());
        assertEquals("Scotland", accountEnquiryDto.getDefendant().getAddressLine1());
        assertEquals("case001", accountEnquiryDto.getPcr());
        assertEquals(fromLocalDate(today), accountEnquiryDto.getDefendant().getBirthDate());
        assertEquals("Dave", accountEnquiryDto.getDefendant().getForenames());
        assertEquals("HRH", accountEnquiryDto.getDefendant().getInitials());
        assertEquals("QUICK", accountEnquiryDto.getSearchType());
        assertEquals("VISA", accountEnquiryDto.getMajorCreditor());
        assertEquals("XX12345678", accountEnquiryDto.getDefendant().getNationalInsuranceNumber());
        assertEquals("6", accountEnquiryDto.getTillNumber());
        assertEquals(today, accountEnquiryDto.getDefendant().getBirthDate().toLocalDate());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder().build())
            .toString());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(DateDto.builder().year(2024).build())
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(DateDto.builder().year(2024).monthOfYear(-1).build())
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(DateDto.builder().year(2024).monthOfYear(2).build())
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(DateDto.builder().year(2024).monthOfYear(4).build())
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(DateDto.builder().year(2024).monthOfYear(6).build())
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(DateDto.builder().year(2024).monthOfYear(8).build())
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(DateDto.builder().year(2024).monthOfYear(9).build())
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(DateDto.builder().year(2024).monthOfYear(11).build())
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(DateDto.builder().year(2024).monthOfYear(13).build())
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(DateDto.builder().year(2024).monthOfYear(6).dayOfMonth(-5).build())
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(DateDto.builder().year(2024).monthOfYear(6).dayOfMonth(5).build())
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(DateDto.builder().year(2024).monthOfYear(6).dayOfMonth(35).build())
                .build())
            .build());

    }

    @Test
    void testToJsonString() throws Exception {
        AccountSearchDto accountEnquiryDto = constructTestAccountSearchDto(LocalDate.now());
        assertNotNull(accountEnquiryDto.toJsonString());
    }

    @Test
    void testControllerModelEqualsAndHashCode() {
        // Arrange
        AccountSearchDto model1 = AccountSearchDto.builder().build();
        AccountSearchDto model2 = AccountSearchDto.builder().build();

        // Assert
        assertEquals(model1, model2);
        assertEquals(model1.hashCode(), model2.hashCode());
    }

    @Test
    void testControllerModelToString() {
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
            .pcr("case001")
            .searchType("QUICK")
            .majorCreditor("VISA")
            .tillNumber("6")
            .defendant(DefendantDto.builder()
                .surname("Smith")
                .forenames("Dave")
                .initials("HRH")
                .addressLine1("Scotland")
                .birthDate(fromLocalDate(today))
                .nationalInsuranceNumber("XX12345678")
                .build())
            .build();
    }
}
