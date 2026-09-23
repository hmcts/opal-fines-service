package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.legacy.AddDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.ContactDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountPartyLegacy;
import uk.gov.hmcts.opal.dto.legacy.IndividualDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LanguagePreferencesLegacy;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;
import uk.gov.hmcts.opal.generated.model.LanguagePreferenceCommonStrict;
import uk.gov.hmcts.opal.generated.model.DefendantAccountParty;
import uk.gov.hmcts.opal.generated.model.PartyResponseDefendantAccount;

class DefendantAccountPartyLegacyResponseMapperTest {

    private final DefendantAccountPartyLegacyResponseMapper mapper =
        Mappers.getMapper(DefendantAccountPartyLegacyResponseMapper.class);

    @Test
    void whenLegacyResponseIsMapped_generatedResponseIsPopulated_happyPath() {
        AddDefendantAccountPartyLegacyResponse legacy = AddDefendantAccountPartyLegacyResponse.builder()
            .version(BigInteger.valueOf(7))
            .defendantAccountParty(DefendantAccountPartyLegacy.builder()
                .defendantAccountPartyType("Defendant")
                .isDebtor(true)
                .partyDetails(PartyDetailsLegacy.builder()
                    .partyId("20010")
                    .organisationFlag(false)
                    .individualDetails(IndividualDetailsLegacy.builder()
                        .forenames("Alex")
                        .surname("Smith")
                        .build())
                    .build())
                .address(AddressDetailsLegacy.builder()
                    .addressLine1("1 High Street")
                    .postcode("AA1 1AA")
                    .build())
                .contactDetails(ContactDetailsLegacy.builder()
                    .primaryEmailAddress("alex@example.com")
                    .build())
                .languagePreferences(LanguagePreferencesLegacy.builder()
                    .documentLanguagePreference(LanguagePreferencesLegacy.LanguagePreference.builder()
                        .languageCode("en")
                        .languageDisplayName("English")
                        .build())
                    .build())
                .build())
            .build();

        PartyResponseDefendantAccount result = mapper.toGeneratedResponse(legacy);
        DefendantAccountParty party = result.getDefendantAccountParty();
        LanguagePreferenceCommonStrict language = party.getLanguagePreferences().get()
            .getDocumentLanguagePreference().get();

        assertAll(
            () -> assertEquals(BigInteger.valueOf(7), result.getVersion()),
            () -> assertNotNull(party),
            () -> assertEquals(DefendantAccountParty.DefendantAccountPartyTypeEnum.DEFENDANT,
                party.getDefendantAccountPartyType()),
            () -> assertEquals("Alex", party.getPartyDetails().getIndividualDetails().get().getForenames().get()),
            () -> assertEquals("1 High Street", party.getAddress().getAddressLine1()),
            () -> assertEquals("alex@example.com",
                party.getContactDetails().get().getPrimaryEmailAddress().get()),
            () -> assertEquals(LanguagePreferenceCommonStrict.LanguageCodeEnum.EN, language.getLanguageCode()),
            () -> assertEquals(LanguagePreferenceCommonStrict.LanguageDisplayNameEnum.ENGLISH_ONLY,
                language.getLanguageDisplayName())
        );
    }
}
