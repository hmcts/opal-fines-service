package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.legacy.AddDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.ContactDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountPartyLegacy;
import uk.gov.hmcts.opal.dto.legacy.LanguagePreferencesLegacy;
import uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails;
import uk.gov.hmcts.opal.generated.model.LanguagePreferenceCommonStrict;
import uk.gov.hmcts.opal.generated.model.DefendantAccountParty;
import uk.gov.hmcts.opal.generated.model.PartyResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommonStrict;

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
                .partyDetails(LegacyPartyDetails.builder()
                    .partyId("20010")
                    .organisationFlag(false)
                    .individualDetails(IndividualDetails.builder()
                        .surname("Alex Smith")
                        .individualAliases(new IndividualDetails.IndividualAlias[] {
                            IndividualDetails.IndividualAlias.builder()
                                .aliasId("30010")
                                .sequenceNumber((short) 1)
                                .surname("Smith")
                                .forenames("Alex")
                                .build()
                        })
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
            () -> assertNull(party.getPartyDetails().getIndividualDetails().get().getForenames().get()),
            () -> assertEquals("Alex Smith", party.getPartyDetails().getIndividualDetails().get().getSurname()),
            () -> assertEquals("30010", party.getPartyDetails().getIndividualDetails().get()
                .getIndividualAliases().get().getFirst().getAliasId()),
            () -> assertEquals("1 High Street", party.getAddress().getAddressLine1()),
            () -> assertEquals("alex@example.com",
                party.getContactDetails().get().getPrimaryEmailAddress().get()),
            () -> assertEquals(LanguagePreferenceCommonStrict.LanguageCodeEnum.EN, language.getLanguageCode()),
            () -> assertEquals(LanguagePreferenceCommonStrict.LanguageDisplayNameEnum.ENGLISH_ONLY,
                language.getLanguageDisplayName())
        );
    }

    @Test
    void mapsOrganisationAliases() {
        PartyDetailsCommonStrict result = mapper.toGeneratedResponse(LegacyPartyDetails.builder()
            .organisationFlag(true)
            .organisationDetails(OrganisationDetails.builder()
                .organisationName("Example Ltd")
                .organisationAliases(new OrganisationDetails.OrganisationAlias[] {
                    OrganisationDetails.OrganisationAlias.builder()
                        .aliasId("40010")
                        .sequenceNumber((short) 1)
                        .organisationName("Example Trading")
                        .build()
                })
                .build())
            .build());

        assertEquals("40010", result.getOrganisationDetails().get().getOrganisationAliases().get()
            .getFirst().getAliasId());
    }
}
