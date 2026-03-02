package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;

@SpringJUnitConfig
@ContextConfiguration(classes = {
    PartyDetailsCommonMapperImpl.class,
    IndividualDetailsCommonMapperImpl.class,
    OrganisationDetailsCommonMapperImpl.class
})
class PartyDetailsCommonMapperTest {

    @Autowired
    private PartyDetailsCommonMapper mapper;

    @Test
    void givenOrganisationParty_whenToPartyDetailsCommon_thenMapsOrganisationOnly() {
        PartyEntity party = PartyEntity.builder()
            .partyId(200L)
            .organisation(true)
            .organisationName("Acme Ltd")
            .build();

        PartyDetailsCommon mapped = mapper.toPartyDetailsCommon(party);

        assertNotNull(mapped);
        assertEquals("200", mapped.getPartyId());
        assertTrue(mapped.getOrganisationFlag());
        assertNotNull(mapped.getOrganisationDetails());
        assertEquals("Acme Ltd", mapped.getOrganisationDetails().getOrganisationName());
        assertNull(mapped.getIndividualDetails());
    }

    @Test
    void givenIndividualParty_whenToPartyDetailsCommon_thenMapsIndividualOnly() {
        PartyEntity party = PartyEntity.builder()
            .partyId(201L)
            .organisation(false)
            .surname("Smith")
            .forenames("John")
            .title("Mr")
            .build();

        PartyDetailsCommon mapped = mapper.toPartyDetailsCommon(party);

        assertNotNull(mapped);
        assertEquals("201", mapped.getPartyId());
        assertEquals(false, mapped.getOrganisationFlag());
        assertNotNull(mapped.getIndividualDetails());
        assertEquals("Smith", mapped.getIndividualDetails().getSurname());
        assertEquals("John", mapped.getIndividualDetails().getForenames());
        assertEquals("Mr", mapped.getIndividualDetails().getTitle());
        assertNull(mapped.getOrganisationDetails());
    }
}
