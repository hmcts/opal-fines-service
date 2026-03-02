package uk.gov.hmcts.opal.mapper.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;

class AddressMapperTest {

    AddressMapper mapper = Mappers.getMapper(AddressMapper.class);

    @Test
    void givenPartyEntity_whenToAddressDetailsCommon_thenMapsAddressFields() {

        //Arrange
        PartyEntity party = PartyEntity.builder()
            .addressLine1("Line 1")
            .addressLine2("Line 2")
            .addressLine3("Line 3")
            .addressLine4("Line 4")
            .addressLine5("Line 5")
            .postcode("AA1 1AA")
            .build();

        // Act
        AddressDetailsCommon mapped = mapper.toAddressDetailsCommon(party);

        // Assert
        assertNotNull(mapped);
        assertEquals("Line 1", mapped.getAddressLine1());
        assertEquals("Line 2", mapped.getAddressLine2());
        assertEquals("Line 3", mapped.getAddressLine3());
        assertEquals("Line 4", mapped.getAddressLine4());
        assertEquals("Line 5", mapped.getAddressLine5());
        assertEquals("AA1 1AA", mapped.getPostcode());
    }
}
