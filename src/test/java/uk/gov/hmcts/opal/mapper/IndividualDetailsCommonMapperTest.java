package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;

@SpringJUnitConfig
@ContextConfiguration(classes = {IndividualDetailsCommonMapperImpl.class})
class IndividualDetailsCommonMapperTest {

    @Autowired
    private IndividualDetailsCommonMapper mapper;

    @Test
    void givenIndividualParty_whenToIndividualDetailsCommon_thenMapsIndividualFields() {
        PartyEntity party = PartyEntity.builder()
            .organisation(false)
            .surname("Smith")
            .forenames("John")
            .title("Mr")
            .build();

        IndividualDetailsCommon mapped = mapper.toIndividualDetailsCommon(party);

        assertNotNull(mapped);
        assertEquals("Smith", mapped.getSurname());
        assertEquals("John", mapped.getForenames());
        assertEquals("Mr", mapped.getTitle());
    }

    @Test
    void givenOrganisationParty_whenToIndividualDetailsWhenPartyIsIndividual_thenReturnsNull() {
        PartyEntity party = PartyEntity.builder()
            .organisation(true)
            .build();

        assertNull(mapper.toIndividualDetailsWhenPartyIsIndividual(party));
    }
}
