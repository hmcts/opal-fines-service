package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;

class MinorCreditorAccountUpdateMapperTest {

    private final MinorCreditorAccountUpdateMapper mapper =
        Mappers.getMapper(MinorCreditorAccountUpdateMapper.class);

    @Test
    void updateParty_whenOrganisationFlagTrue_clearsIndividualFields() {
        // Arrange
        PartyEntity party = PartyEntity.builder()
            .title("Mr")
            .forenames("John")
            .surname("Smith")
            .birthDate(LocalDate.of(1990, 1, 1))
            .age((short) 34)
            .niNumber("QQ123456C")
            .organisationName("Old Org")
            .build();

        PartyDetailsCommon partyDetails = new PartyDetailsCommon()
            .organisationFlag(true)
            .organisationDetails(new OrganisationDetailsCommon().organisationName("Minor Creditor Test Ltd"))
            .individualDetails(new IndividualDetailsCommon()
                                   .title("Should")
                                   .forenames("Be")
                                   .surname("Cleared")
                                   .dateOfBirth("2000-01-01")
                                   .age("20")
                                   .nationalInsuranceNumber("AA123456A"));

        AddressDetailsCommon address = new AddressDetailsCommon()
            .addressLine1("1 Test Street")
            .postcode("ZZ1 1ZZ");

        // Act
        mapper.updateParty(partyDetails, address, party);

        // Assert
        assertTrue(party.isOrganisation());
        assertEquals("Minor Creditor Test Ltd", party.getOrganisationName());
        assertNull(party.getTitle());
        assertNull(party.getForenames());
        assertNull(party.getSurname());
        assertNull(party.getBirthDate());
        assertNull(party.getAge());
        assertNull(party.getNiNumber());
    }

    @Test
    void updateParty_whenOrganisationFlagFalse_clearsOrganisationNameAndMapsIndividualFields() {
        // Arrange
        PartyEntity party = PartyEntity.builder()
            .organisation(true)
            .organisationName("Old Org Name")
            .build();

        PartyDetailsCommon partyDetails = new PartyDetailsCommon()
            .organisationFlag(false)
            .individualDetails(new IndividualDetailsCommon()
                                   .title("Ms")
                                   .forenames("Jane")
                                   .surname("Doe")
                                   .dateOfBirth("2001-02-03")
                                   .age("23")
                                   .nationalInsuranceNumber("QQ654321C"));

        AddressDetailsCommon address = new AddressDetailsCommon()
            .addressLine1("2 New Street")
            .addressLine2("Town")
            .postcode("AA1 1AA");

        // Act
        mapper.updateParty(partyDetails, address, party);

        // Assert
        assertTrue(!party.isOrganisation());
        assertNull(party.getOrganisationName());
        assertEquals("Ms", party.getTitle());
        assertEquals("Jane", party.getForenames());
        assertEquals("Doe", party.getSurname());
        assertEquals(LocalDate.of(2001, 2, 3), party.getBirthDate());
        assertEquals((short) 23, party.getAge());
        assertEquals("QQ654321C", party.getNiNumber());
        assertEquals("2 New Street", party.getAddressLine1());
        assertEquals("Town", party.getAddressLine2());
        assertEquals("AA1 1AA", party.getPostcode());
    }

    @Test
    void parseBirthDate_whenNullOrBlank_returnsNull() {
        // Arrange / Act / Assert
        assertNull(mapper.parseBirthDate(null));
        assertNull(mapper.parseBirthDate(" "));
    }

    @Test
    void parseBirthDate_whenInvalid_throwsIllegalArgumentException() {
        // Act
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> mapper.parseBirthDate("09/09/2000")
        );

        // Assert
        assertEquals("Invalid individual_details.date_of_birth format", exception.getMessage());
    }

    @Test
    void parseAge_whenNullBlankOrInvalid_handlesExpectedCases() {
        // Arrange / Act / Assert
        assertNull(mapper.parseAge(null));
        assertNull(mapper.parseAge(" "));
        assertEquals((short) 25, mapper.parseAge("25"));

        // Act
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> mapper.parseAge("age")
        );
        // Assert
        assertEquals("Invalid individual_details.age format", exception.getMessage());
    }
}
