package uk.gov.hmcts.opal.mapper.legacy;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.legacy.AddDefendantAccountPartyLegacyResponse;
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
        assertNull(mapped.getDefendantAccountParty().getPartyDetails().getOrganisationFlag());
        assertNull(mapped.getDefendantAccountParty().getPartyDetails().getOrganisationDetails());
        assertNull(mapped.getDefendantAccountParty().getPartyDetails().getIndividualDetails());
        assertNull(mapped.getDefendantAccountParty().getAddress());
        assertNull(mapped.getDefendantAccountParty().getContactDetails());
        assertNull(mapped.getDefendantAccountParty().getVehicleDetails());
        assertNull(mapped.getDefendantAccountParty().getEmployerDetails());
        assertNull(mapped.getDefendantAccountParty().getLanguagePreferences());
    }


    @Test
    void defendantAccountParty_mapsLanguagePreference() {

        //Arrange
        AddDefendantAccountPartyLegacyResponse legacyBody = AddDefendantAccountPartyLegacyResponse.builder()
            .version(5)
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
                    .languagePreferences(
                        LanguagePreferencesLegacy.builder()
                            .documentLanguagePreference(
                                LanguagePreferencesLegacy.LanguagePreference.builder()
                                    .languageCode("en")
                                    .languageDisplayName("English")
                                    .build()
                            )
                            .hearingLanguagePreference(
                                LanguagePreferencesLegacy.LanguagePreference.builder()
                                    .languageCode("EN")
                                    .languageDisplayName("English")
                                    .build()
                            )
                            .build()
                    )
                    .build()
                )
            .build();


        //Act
        GetDefendantAccountPartyResponse mapped = mapper.toDefendantAccountPartyResponse(legacyBody);

        // Assert
        assertNotNull(mapped);
        assertEquals(BigInteger.valueOf(5), mapped.getVersion());
        assertNotNull(mapped.getDefendantAccountParty());

        // language preferences should be mapped and use codes (document -> "en", hearing -> "fr")
        assertNotNull(mapped.getDefendantAccountParty().getLanguagePreferences(),
                      "Language preferences should be mapped when provided by legacy");
        assertEquals(
            "EN",
            mapped.getDefendantAccountParty().getLanguagePreferences().getDocumentLanguagePreference().getLanguageCode()
        );
    }


    /*@Test
    void LanguagePreference_returnsNotNull_whenLegacyIsNotNulls() {

        //Arrange
        AddDefendantAccountPartyLegacyResponse legacyBody = AddDefendantAccountPartyLegacyResponse.builder()
            .version(6)
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
                    .languagePreferences(assertNotNull()) // explicitly null
                    .build()
            )
            .build();


        //Act
        GetDefendantAccountPartyResponse mapped = mapper.toDefendantAccountPartyResponse(legacyBody);

        // Assert
        assertNotNull(mapped);
        assertEquals(BigInteger.valueOf(6), mapped.getVersion());
        assertNotNull(mapped.getDefendantAccountParty());

        // language preferences should be null in modern model when legacy had none
        assertNull(mapped.getDefendantAccountParty().getLanguagePreferences(),
                   "Language preferences should be null when legacy languagePreferences is null");
    }*/


    @Test
    void languagePreference_returnsNull_whenLegacyIsNulls() {

        //Arrange
        AddDefendantAccountPartyLegacyResponse legacyBody = AddDefendantAccountPartyLegacyResponse.builder()
            .version(6)
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
                    .languagePreferences(null) // explicitly null
                    .build()
            )
            .build();


        //Act
        GetDefendantAccountPartyResponse mapped = mapper.toDefendantAccountPartyResponse(legacyBody);

        // Assert
        assertNotNull(mapped);
        assertEquals(BigInteger.valueOf(6), mapped.getVersion());
        assertNotNull(mapped.getDefendantAccountParty());

        // language preferences should be null in modern model when legacy had none
        assertNull(mapped.getDefendantAccountParty().getLanguagePreferences(),
                   "Language preferences should be null when legacy languagePreferences is null");
    }
}

