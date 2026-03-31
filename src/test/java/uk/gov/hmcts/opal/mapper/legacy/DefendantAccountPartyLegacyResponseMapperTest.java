package uk.gov.hmcts.opal.mapper.legacy;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.legacy.AddDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountPartyLegacy;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefendantAccountPartyLegacyResponseMapperTest {
    private final DefendantAccountPartyLegacyResponseMapper mapper = Mappers.getMapper(
        DefendantAccountPartyLegacyResponseMapper.class);

    @Test
    void DefendantAccountPartyResponse_mapsNullNestedObjects_toNulls() {

        //Arrange
        AddDefendantAccountPartyLegacyResponse legacyBody = AddDefendantAccountPartyLegacyResponse.builder()
            .version(4)
            .defendantAccountParty(
                DefendantAccountPartyLegacy.builder()
                    .defendantAccountPartyType("Defendant")
                    .isDebtor(false)
                    // partyDetails present but with nested organisation and individual null
                    .partyDetails(
                        PartyDetailsLegacy.builder()
                            .partyId("20010")
                            .organisationFlag(null) // intentionally null -> modern should be null
                            .organisationDetails(null)
                            .individualDetails(null)
                            .build()
                    )
                    // address, contact, vehicle, employer, languagePreferences all null
                    .address(null)
                    .contactDetails(null)
                    .vehicleDetails(null)
                    .employerDetails(null)
                    .languagePreferences(null)
                    .build()
            )
            .build();


        //Act
        GetDefendantAccountPartyResponse mapped = mapper.toDefendantAccountPartyResponse(legacyBody);

        //Assert
        assertEquals(null, mapped.getDefendantAccountParty().getPartyDetails().getOrganisationFlag());
        assertEquals(null, mapped.getDefendantAccountParty().getPartyDetails().getOrganisationDetails());
        assertEquals(null, mapped.getDefendantAccountParty().getPartyDetails().getIndividualDetails());
        assertEquals(null, mapped.getDefendantAccountParty().getAddress());
        assertEquals(null, mapped.getDefendantAccountParty().getContactDetails());
        assertEquals(null, mapped.getDefendantAccountParty().getVehicleDetails());
        assertEquals(null, mapped.getDefendantAccountParty().getEmployerDetails());
        assertEquals(null, mapped.getDefendantAccountParty().getLanguagePreferences());
    }

}

