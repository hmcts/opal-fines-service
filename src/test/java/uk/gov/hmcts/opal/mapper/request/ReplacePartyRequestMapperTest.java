package uk.gov.hmcts.opal.mapper.request;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountParty;
import uk.gov.hmcts.opal.generated.model.LanguagePreferencesCommonStrict;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyContactDetailsDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PartyEmployerDetailsDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PartyVehicleDetailsDefendantAccount;
import uk.gov.hmcts.opal.generated.model.ReplacePartyRequestDefendantAccount;

class ReplacePartyRequestMapperTest {

    private final ReplacePartyRequestMapper mapper = Mappers.getMapper(ReplacePartyRequestMapper.class);

    @Nested
    class ToDefendantAccountParty {

        @Test
        void whenReplacePartyRequestIsMapped_partyFieldsArePreserved_happyPath() {
            PartyContactDetailsDefendantAccount contactDetails = new PartyContactDetailsDefendantAccount();
            PartyVehicleDetailsDefendantAccount vehicleDetails = new PartyVehicleDetailsDefendantAccount();
            PartyEmployerDetailsDefendantAccount employerDetails = new PartyEmployerDetailsDefendantAccount();
            LanguagePreferencesCommonStrict languagePreferences = new LanguagePreferencesCommonStrict();
            ReplacePartyRequestDefendantAccount request = ReplacePartyRequestDefendantAccount.builder()
                .defendantAccountPartyType(
                    ReplacePartyRequestDefendantAccount.DefendantAccountPartyTypeEnum.DEFENDANT)
                .isDebtor(true)
                .contactDetails(JsonNullable.of(contactDetails))
                .vehicleDetails(JsonNullable.of(vehicleDetails))
                .employerDetails(JsonNullable.of(employerDetails))
                .languagePreferences(JsonNullable.of(languagePreferences))
                .build();

            DefendantAccountParty result = mapper.toDefendantAccountParty(request);

            assertAll(
                () -> assertEquals(DefendantAccountParty.DefendantAccountPartyTypeEnum.DEFENDANT,
                    result.getDefendantAccountPartyType()),
                () -> assertEquals(true, result.getIsDebtor()),
                () -> assertSame(contactDetails, result.getContactDetails().get()),
                () -> assertSame(vehicleDetails, result.getVehicleDetails().get()),
                () -> assertSame(employerDetails, result.getEmployerDetails().get()),
                () -> assertSame(languagePreferences, result.getLanguagePreferences().get())
            );
        }

        @Test
        void whenReplacePartyRequestUsesCommonPartyAndAddress_theyAreMappedToStrictModels_happyPath() {
            ReplacePartyRequestDefendantAccount request = ReplacePartyRequestDefendantAccount.builder()
                .partyDetails(PartyDetailsCommon.builder().partyId("123").organisationFlag(false).build())
                .address(AddressDetailsCommon.builder().addressLine1("1 High Street").build())
                .build();

            DefendantAccountParty result = mapper.toDefendantAccountParty(request);

            assertAll(
                () -> assertEquals("123", result.getPartyDetails().getPartyId()),
                () -> assertEquals(false, result.getPartyDetails().getOrganisationFlag()),
                () -> assertEquals("1 High Street", result.getAddress().getAddressLine1()),
                () -> assertEquals(null, result.getAddress().getAddressLine2().get())
            );
        }
    }
}
