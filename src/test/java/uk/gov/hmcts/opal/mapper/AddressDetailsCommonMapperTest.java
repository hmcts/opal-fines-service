package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;

@SpringJUnitConfig
@ContextConfiguration(classes = {AddressDetailsCommonMapperImpl.class})
class AddressDetailsCommonMapperTest {

    @Autowired
    private AddressDetailsCommonMapper mapper;

    @Test
    void givenPartyEntity_whenToAddressDetailsCommon_thenMapsAddressFields() {
        PartyEntity party = PartyEntity.builder()
            .addressLine1("Line 1")
            .addressLine2("Line 2")
            .addressLine3("Line 3")
            .addressLine4("Line 4")
            .addressLine5("Line 5")
            .postcode("AA1 1AA")
            .build();

        AddressDetailsCommon mapped = mapper.toAddressDetailsCommon(party);

        assertNotNull(mapped);
        assertEquals("Line 1", mapped.getAddressLine1());
        assertEquals("Line 2", mapped.getAddressLine2());
        assertEquals("Line 3", mapped.getAddressLine3());
        assertEquals("Line 4", mapped.getAddressLine4());
        assertEquals("Line 5", mapped.getAddressLine5());
        assertEquals("AA1 1AA", mapped.getPostcode());
    }
}
