package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommon;

@SpringJUnitConfig
@ContextConfiguration(classes = {OrganisationDetailsCommonMapperImpl.class})
class OrganisationDetailsCommonMapperTest {

    @Autowired
    private OrganisationDetailsCommonMapper mapper;

    @Test
    void givenOrganisationParty_whenToOrganisationDetailsCommon_thenMapsOrganisationName() {
        PartyEntity party = PartyEntity.builder()
            .organisation(true)
            .organisationName("Acme Ltd")
            .build();

        OrganisationDetailsCommon mapped = mapper.toOrganisationDetailsCommon(party);

        assertNotNull(mapped);
        assertEquals("Acme Ltd", mapped.getOrganisationName());
    }

    @Test
    void givenIndividualParty_whenToOrganisationDetailsWhenPartyIsOrganisation_thenReturnsNull() {
        PartyEntity party = PartyEntity.builder()
            .organisation(false)
            .build();

        assertNull(mapper.toOrganisationDetailsWhenPartyIsOrganisation(party));
    }
}
