package uk.gov.hmcts.opal.mapper.response;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.debtordetail.Language;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.generated.model.LanguagePreferenceCommonStrict;
import uk.gov.hmcts.opal.generated.model.PartyDefendantAccount;
import uk.gov.hmcts.opal.mapper.AbstractMapperTest;

class DefendantAccountPartyEntityResponseMapperTest extends AbstractMapperTest {

    @Autowired
    private DefendantAccountPartyEntityResponseMapper mapper;

    @Test
    void whenIndividualEntityIsMapped_generatedPartyIsPopulated_happyPath() {
        PartyEntity party = PartyEntity.builder()
            .partyId(123L)
            .organisation(false)
            .title("Mr")
            .forenames("Alex")
            .surname("Smith")
            .birthDate(LocalDate.of(1990, 1, 2))
            .age((short) 36)
            .addressLine1("1 High Street")
            .build();
        DefendantAccountPartiesEntity association = DefendantAccountPartiesEntity.builder()
            .associationType(AssociationType.DEFENDANT)
            .debtor(true)
            .party(party)
            .build();
        DebtorDetailEntity debtorDetail = DebtorDetailEntity.builder()
            .documentLanguage(Language.ENGLISH)
            .hearingLanguage(Language.WELSH_AND_ENGLISH)
            .build();
        AliasEntity alias = AliasEntity.builder()
            .aliasId(10L)
            .sequenceNumber(1)
            .forenames("A")
            .surname("Jones")
            .build();

        PartyDefendantAccount result = mapper.toGeneratedResponse(association, debtorDetail, List.of(alias));

        assertAll(
            () -> assertEquals(PartyDefendantAccount.DefendantAccountPartyTypeEnum.DEFENDANT,
                result.getDefendantAccountPartyType()),
            () -> assertTrue(result.getIsDebtor()),
            () -> assertEquals("123", result.getPartyDetails().getPartyId()),
            () -> assertEquals("Alex", result.getPartyDetails().getIndividualDetails().get().getForenames().get()),
            () -> assertEquals("Jones", result.getPartyDetails().getIndividualDetails().get()
                .getIndividualAliases().get().getFirst().getSurname()),
            () -> assertEquals("1 High Street", result.getAddress().getAddressLine1()),
            () -> assertEquals(LanguagePreferenceCommonStrict.LanguageCodeEnum.EN,
                result.getLanguagePreferences().get().getDocumentLanguagePreference().get().getLanguageCode()),
            () -> assertEquals(LanguagePreferenceCommonStrict.LanguageDisplayNameEnum.WELSH_AND_ENGLISH,
                result.getLanguagePreferences().get().getHearingLanguagePreference().get().getLanguageDisplayName())
        );
    }

    @Test
    void whenDebtorDetailIsMissing_requiredNullableObjectsContainExplicitNulls_happyPath() {
        PartyEntity party = PartyEntity.builder()
            .partyId(123L)
            .organisation(true)
            .organisationName("Example Ltd")
            .addressLine1("1 High Street")
            .build();
        DefendantAccountPartiesEntity association = DefendantAccountPartiesEntity.builder()
            .associationType(AssociationType.DEFENDANT)
            .debtor(false)
            .party(party)
            .build();

        PartyDefendantAccount result = mapper.toGeneratedResponse(association, null, List.of());

        assertAll(
            () -> assertNull(result.getVehicleDetails().get().getVehicleMakeAndModel().get()),
            () -> assertNull(result.getVehicleDetails().get().getVehicleRegistration().get()),
            () -> assertNull(result.getEmployerDetails().get().getEmployerName().get()),
            () -> assertEquals("", result.getEmployerDetails().get().getEmployerAddress().getAddressLine1()),
            () -> assertNull(result.getEmployerDetails().get().getEmployerAddress().getAddressLine2().get()),
            () -> assertNull(result.getLanguagePreferences().get().getDocumentLanguagePreference().get()
                .getLanguageCode()),
            () -> assertNull(result.getLanguagePreferences().get().getHearingLanguagePreference().get()
                .getLanguageCode())
        );
    }
}
