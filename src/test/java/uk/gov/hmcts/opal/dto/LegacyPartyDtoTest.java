package uk.gov.hmcts.opal.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LegacyPartyDtoTest {

    @Test
    void testBuilder() {
        PartyDto partyDto = constructTestPartyDto();

        assertEquals(Long.valueOf(1L), partyDto.getPartyId());
        assertTrue(partyDto.isOrganisation());
        assertEquals("Big Acme Corporation", partyDto.getOrganisationName());
        assertEquals("Smith", partyDto.getSurname());
        assertEquals("John James", partyDto.getForenames());
        assertEquals("JJ", partyDto.getInitials());
        assertEquals("Mr", partyDto.getTitle());
        assertEquals("22 Acacia Avenue", partyDto.getAddressLine1());
        assertEquals("Hammersmith", partyDto.getAddressLine2());
        assertEquals("Birmingham", partyDto.getAddressLine3());
        assertEquals("Cornwall", partyDto.getAddressLine4());
        assertEquals("Scotland", partyDto.getAddressLine5());
        assertEquals("SN15 9TT", partyDto.getPostcode());
        assertEquals("TFO", partyDto.getAccountType());
        assertEquals(LocalDate.of(2001, 8, 16), partyDto.getDateOfBirth());
        assertEquals(Short.valueOf((short) 21), partyDto.getAge());
        assertEquals("FF22446688", partyDto.getNiNumber());
        assertEquals(LocalDateTime.of(2023, 12, 5, 15, 45), partyDto.getLastChangedDate());

        assertNotNull(AccountEnquiryDto.builder().toString());
    }

    @Test
    void testToJsonString() throws Exception {
        PartyDto partyDto = constructTestPartyDto();

        assertNotNull(partyDto.toJsonString());
    }

    @Test
    void testControllerModelEqualsAndHashCode() {
        // Arrange
        PartyDto model1 = PartyDto.builder().build();
        PartyDto model2 = PartyDto.builder().build();

        // Assert
        assertEquals(model1, model2);
        assertEquals(model1.hashCode(), model2.hashCode());
    }

    @Test
    void testControllerModelToString() {
        // Arrange
        PartyDto model = PartyDto.builder().build();

        // Act
        String result = model.toString();

        // Assert
        assertNotNull(result);
    }

    private PartyDto constructTestPartyDto() {
        return PartyDto.builder()
            .partyId(1L)
            .organisation(true)
            .organisationName("Big Acme Corporation")
            .surname("Smith")
            .forenames("John James")
            .initials("JJ")
            .title("Mr")
            .addressLine1("22 Acacia Avenue")
            .addressLine2("Hammersmith")
            .addressLine3("Birmingham")
            .addressLine4("Cornwall")
            .addressLine5("Scotland")
            .postcode("SN15 9TT")
            .accountType("TFO")  // TFO = Transfer. Could also be FP = Fixed Penalty
            .dateOfBirth(LocalDate.of(2001, 8, 16))
            .age((short)21)
            .niNumber("FF22446688")
            .lastChangedDate(LocalDateTime.of(2023, 12, 5, 15, 45))
            .build();
    }
}
