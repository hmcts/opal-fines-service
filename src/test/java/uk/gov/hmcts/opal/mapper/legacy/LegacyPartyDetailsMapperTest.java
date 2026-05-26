package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.legacy.IndividualDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.OrganisationDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;
import uk.gov.hmcts.opal.mapper.AbstractMapperTest;

class LegacyPartyDetailsMapperTest extends AbstractMapperTest {

    @Autowired
    private LegacyPartyDetailsMapper mapper;

    @Test
    void givenLegacyPartyDetails_whenToOpal_thenMapsExpectedFields() {
        // Arrange
        OrganisationDetailsLegacy organisationDetails = OrganisationDetailsLegacy.builder()
            .organisationName("Acme Ltd")
            .build();
        IndividualDetailsLegacy individualDetails = IndividualDetailsLegacy.builder()
            .title("Ms")
            .forenames("Jane")
            .surname("Smith")
            .dateOfBirth("1985-04-17")
            .age("40")
            .nationalInsuranceNumber("QQ123456C")
            .build();
        PartyDetailsLegacy legacy = PartyDetailsLegacy.builder()
            .partyId("20010")
            .organisationFlag(false)
            .organisationDetails(organisationDetails)
            .individualDetails(individualDetails)
            .build();

        // Act
        PartyDetails mapped = mapper.toOpal(legacy);

        // Assert
        assertNotNull(mapped);
        assertEquals("20010", mapped.getPartyId());
        assertEquals(false, mapped.getOrganisationFlag());

        OrganisationDetails mappedOrganisation = mapped.getOrganisationDetails();
        assertNotNull(mappedOrganisation);
        assertEquals("Acme Ltd", mappedOrganisation.getOrganisationName());

        IndividualDetails mappedIndividual = mapped.getIndividualDetails();
        assertNotNull(mappedIndividual);
        assertEquals("Ms", mappedIndividual.getTitle());
        assertEquals("Jane", mappedIndividual.getForenames());
        assertEquals("Smith", mappedIndividual.getSurname());
        assertEquals("1985-04-17", mappedIndividual.getDateOfBirth());
        assertEquals("40", mappedIndividual.getAge());
        assertEquals("QQ123456C", mappedIndividual.getNationalInsuranceNumber());
    }
}
