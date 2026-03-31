package uk.gov.hmcts.opal.mapper.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;

@SpringJUnitConfig
@ContextConfiguration(classes = {
    PartyMapperImpl.class,
    IndividualDetailsMapperImpl.class,
    OrganisationDetailsMapperImpl.class
})
@Isolated
class PartyMapperTest {

    @Autowired
    private PartyMapper mapper;

    @Test
    void givenOrganisationParty_whenToPartyDetailsCommon_thenMapsOrganisationOnly() {

        // Arrange
        PartyEntity party = PartyEntity.builder()
            .partyId(200L)
            .organisation(true)
            .organisationName("Acme Ltd")
            .build();

        // Act
        PartyDetailsCommon mapped = mapper.toPartyDetailsCommon(party);

        // Assert
        assertNotNull(mapped);
        assertEquals("200", mapped.getPartyId());
        assertTrue(mapped.getOrganisationFlag());
        assertNotNull(mapped.getOrganisationDetails());
        assertEquals("Acme Ltd", mapped.getOrganisationDetails().getOrganisationName());
        assertNull(mapped.getIndividualDetails());
    }

    @Test
    void givenIndividualParty_whenToPartyDetailsCommon_thenMapsIndividualOnly() {

        // Arrange
        PartyEntity party = PartyEntity.builder()
            .partyId(201L)
            .organisation(false)
            .surname("Smith")
            .forenames("John")
            .title("Mr")
            .build();

        // Act
        PartyDetailsCommon mapped = mapper.toPartyDetailsCommon(party);

        // Assert
        assertNotNull(mapped);
        assertEquals("201", mapped.getPartyId());
        assertEquals(false, mapped.getOrganisationFlag());
        assertNotNull(mapped.getIndividualDetails());
        assertEquals("Smith", mapped.getIndividualDetails().getSurname());
        assertEquals("John", mapped.getIndividualDetails().getForenames());
        assertEquals("Mr", mapped.getIndividualDetails().getTitle());
        assertNull(mapped.getOrganisationDetails());
    }

    @Test
    void shouldMapIndividualPartyEntityToPartyDetailsDto() {
        // given
        PartyEntity party = PartyEntity.builder()
            .partyId(42L)
            .organisation(false)
            .title("Sir")
            .forenames("Ben")
            .surname("Kenobi")
            .addressLine1("Tatooine Lane")
            .addressLine2("Mos Eisley")
            .postcode("TAT 1OO")
            .build();

        // when
        PartyDetails dto = mapper.toDto(party);

        // then
        assertNotNull(dto, "DTO should not be null");
        assertEquals("42", dto.getPartyId());
        assertFalse(dto.getOrganisationFlag(), "organisationFlag should be false for individuals");

        assertNull(dto.getOrganisationDetails(), "organisationDetails should be null for individual party");

        IndividualDetails individual = dto.getIndividualDetails();
        assertNotNull(individual, "individualDetails must be mapped for a non-organisation party");
        assertEquals("Sir", individual.getTitle());
        assertEquals("Ben", individual.getForenames());
        assertEquals("Kenobi", individual.getSurname());
    }

    @Test
    void shouldMapOrganisationPartyEntityToPartyDetailsDto() {
        // given
        PartyEntity party = PartyEntity.builder()
            .partyId(100L)
            .organisation(true)
            .organisationName("Galactic Enterprises Ltd")
            .addressLine1("The Spire")
            .postcode("GAL 100")
            .build();

        // when
        PartyDetails dto = mapper.toDto(party);

        // then
        assertNotNull(dto, "DTO should not be null");
        assertEquals("100", dto.getPartyId());
        assertTrue(dto.getOrganisationFlag(), "organisationFlag should be true for organisations");

        OrganisationDetails org = dto.getOrganisationDetails();
        assertNotNull(org, "organisationDetails should be present for organisation parties");
        assertEquals("Galactic Enterprises Ltd", org.getOrganisationName());

        assertNull(dto.getIndividualDetails(), "individualDetails should be null for organisation party");
    }
}
