package uk.gov.hmcts.opal.mapper.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommon;

class OrganisationDetailsMapperTest {

    OrganisationDetailsMapper mapper = Mappers.getMapper(OrganisationDetailsMapper.class);

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
}
