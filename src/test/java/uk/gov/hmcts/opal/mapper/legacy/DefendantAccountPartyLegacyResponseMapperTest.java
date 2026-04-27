package uk.gov.hmcts.opal.mapper.legacy;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.common.LanguagePreference;
import uk.gov.hmcts.opal.dto.legacy.RemoveDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountPartyLegacy;
import uk.gov.hmcts.opal.dto.legacy.LanguagePreferencesLegacy;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefendantAccountPartyLegacyResponseMapperTest {
    private final DefendantAccountPartyLegacyResponseMapper mapper = Mappers.getMapper(
        DefendantAccountPartyLegacyResponseMapper.class);

    @Test
    void defendantAccountPartyResponse_mapsNullNestedObjects_toNulls() {

        //Arrange
        RemoveDefendantAccountPartyLegacyResponse legacyBody = RemoveDefendantAccountPartyLegacyResponse.builder()
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
        assertNull(mapped.getDefendantAccountParty().getPartyDetails().getOrganisationFlag());
        assertNull(mapped.getDefendantAccountParty().getPartyDetails().getOrganisationDetails());
        assertNull(mapped.getDefendantAccountParty().getPartyDetails().getIndividualDetails());
        assertNull(mapped.getDefendantAccountParty().getAddress());
        assertNull(mapped.getDefendantAccountParty().getContactDetails());
        assertNull(mapped.getDefendantAccountParty().getVehicleDetails());
        assertNull(mapped.getDefendantAccountParty().getEmployerDetails());
        assertNull(mapped.getDefendantAccountParty().getLanguagePreferences());
    }

}
