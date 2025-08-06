package uk.gov.hmcts.opal.dto;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.legacy.ReferenceNumberDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantDto;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j(topic = "opal.AccountSearchDtoTest")
public class AccountSearchDtoTest {

    @Test
    void testBuilder() {
        final String dateOfBirth = "2024-01-01";
        AccountSearchDto accountEnquiryDto = constructTestAccountSearchDto(dateOfBirth);
        assertEquals("Bath", accountEnquiryDto.getBusinessUnitIds());
        assertEquals("Smith", accountEnquiryDto.getDefendant().getSurname());
        assertEquals("Scotland", accountEnquiryDto.getDefendant().getAddressLine1());
        assertEquals("case001", accountEnquiryDto.getReferenceNumberDto().getProsecutorCaseReference());
        assertEquals(dateOfBirth, accountEnquiryDto.getDefendant().getBirthDate());
        assertEquals("Dave", accountEnquiryDto.getDefendant().getForenames());
        assertEquals("DS", accountEnquiryDto.getDefendant().getInitials());
        assertEquals("XX12345678", accountEnquiryDto.getDefendant().getNationalInsuranceNumber());
        assertEquals(dateOfBirth, accountEnquiryDto.getDefendant().getBirthDate());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder().build())
            .toString());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(LocalDate.parse("2024-01-01"))
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(LocalDate.parse("2024-01-01"))
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(LocalDate.parse("2024-01-01"))
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(LocalDate.parse("2024-01-01"))
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(LocalDate.parse("2024-01-01"))
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(LocalDate.parse("2024-01-01"))
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(LocalDate.parse("2024-01-01"))
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(LocalDate.parse("2024-01-01"))
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(LocalDate.parse("2024-01-01"))
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(LocalDate.parse("2024-01-01"))
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(LocalDate.parse("2024-01-01"))
                .build())
            .build());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(LocalDate.parse("2024-01-01"))
                .build())
            .build());

    }

    @Test
    void testToJsonString() throws Exception {
        AccountSearchDto accountEnquiryDto = constructTestAccountSearchDto("2024-01-01");
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

    private AccountSearchDto constructTestAccountSearchDto(final String datOfBirth) {
        return AccountSearchDto.builder()
            .businessUnitIds(Collections.singletonList(78))
            .activeAccountsOnly(true)
            .referenceNumberDto(
                ReferenceNumberDto.builder()
                    .prosecutorCaseReference("case001")
                    .organisation(false)
                    .accountNumber(null)
                    .build()
            )
            .defendant(null)
            .build();
    }

}
