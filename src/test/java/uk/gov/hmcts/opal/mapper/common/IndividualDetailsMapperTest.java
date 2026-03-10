package uk.gov.hmcts.opal.mapper.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;

class IndividualDetailsMapperTest {

    IndividualDetailsMapper mapper = Mappers.getMapper(IndividualDetailsMapper.class);

    @Test
    void givenIndividualParty_whenToIndividualDetailsCommon_thenMapsIndividualFields() {

        // Arrange
        PartyEntity party = PartyEntity.builder()
            .organisation(false)
            .surname("Smith")
            .forenames("John")
            .title("Mr")
            .build();

        // Act
        IndividualDetailsCommon mapped = mapper.toIndividualDetailsCommon(party);

        // Assert
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
