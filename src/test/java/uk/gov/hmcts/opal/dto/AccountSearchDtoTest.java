package uk.gov.hmcts.opal.dto;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.legacy.ReferenceNumberDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantDto;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j(topic = "opal.AccountSearchDtoTest")
public class AccountSearchDtoTest {

    @Test
    void testBuilder() {
        final String dateOfBirth = "2024-01-01";
        AccountSearchDto accountEnquiryDto = constructTestAccountSearchDto(dateOfBirth, true);
        assertEquals(List.of((short)78), accountEnquiryDto.getBusinessUnitIds());
        assertEquals("Smith", accountEnquiryDto.getDefendant().getSurname());
        assertEquals("Scotland", accountEnquiryDto.getDefendant().getAddressLine1());
        assertEquals(LocalDate.parse(dateOfBirth), accountEnquiryDto.getDefendant().getBirthDate());
        assertEquals("Dave", accountEnquiryDto.getDefendant().getForenames());
        assertEquals("DS", accountEnquiryDto.getDefendant().getInitials());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder().build())
            .toString());

        assertNotNull(AccountSearchDto.builder()
            .defendant(DefendantDto.builder()
                .birthDate(LocalDate.parse("2024-01-01"))
                .build())
            .build());
    }

    @Test
    void testToJsonString() throws Exception {
        AccountSearchDto accountEnquiryDto = constructTestAccountSearchDto("2024-01-01",true);
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

    private AccountSearchDto constructTestAccountSearchDto(String dateOfBirth, boolean useDefendant) {
        AccountSearchDto.AccountSearchDtoBuilder builder = AccountSearchDto.builder()
            .businessUnitIds(Collections.singletonList((short)78))
            .activeAccountsOnly(true);

        if (useDefendant) {
            builder.defendant(
                DefendantDto.builder()
                    .surname("Smith")
                    .forenames("Dave")
                    .addressLine1("Scotland")
                    .includeAliases(true)
                    .organisation(false)
                    .birthDate(LocalDate.parse(dateOfBirth))
                    .nationalInsuranceNumber("XX12345678")
                    .build()
            );
        } else {
            builder.referenceNumberDto(
                ReferenceNumberDto.builder()
                    .prosecutorCaseReference("ABC123")
                    .organisation(false)
                    .accountNumber("ACC789")
                    .build()
            );
        }

        return builder.build();
    }

}
