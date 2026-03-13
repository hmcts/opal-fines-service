package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.common.EmployerDetails;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountPartyLegacy;
import uk.gov.hmcts.opal.dto.legacy.EmployerDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LanguagePreferencesLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyReplaceDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyReplaceDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;

class LegacyDefAccServiceReplacePartyTest extends AbstractLegacyDefAccServiceTest {

    @Test
    void replaceDefendantAccountParty_mapsNullNestedObjects_toNulls() {
        LegacyReplaceDefendantAccountPartyResponse legacyBody = LegacyReplaceDefendantAccountPartyResponse.builder()
            .version(4)
            .defendantAccountParty(
                DefendantAccountPartyLegacy.builder()
                    .defendantAccountPartyType("Defendant")
                    .isDebtor(false)
                    .partyDetails(
                        PartyDetailsLegacy.builder()
                            .partyId("20010")
                            .organisationFlag(null)
                            .organisationDetails(null)
                            .individualDetails(null)
                            .build()
                    )
                    .address(null)
                    .contactDetails(null)
                    .vehicleDetails(null)
                    .employerDetails(null)
                    .languagePreferences(null)
                    .build()
            )
            .build();

        GatewayService.Response<LegacyReplaceDefendantAccountPartyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacyBody, null, null);

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.REPLACE_DEFENDANT_ACCOUNT_PARTY),
            eq(LegacyReplaceDefendantAccountPartyResponse.class),
            any(LegacyReplaceDefendantAccountPartyRequest.class),
            Mockito.nullable(String.class)
        );

        GetDefendantAccountPartyResponse out = legacyDefendantAccountService.replaceDefendantAccountParty(
            77L, 20010L, null, "1", "78", "poster", "dev_user"
        );

        assertNotNull(out);
        assertEquals(BigInteger.valueOf(4), out.getVersion());
        assertNotNull(out.getDefendantAccountParty());
        assertNotNull(out.getDefendantAccountParty().getPartyDetails());
        assertNull(out.getDefendantAccountParty().getPartyDetails().getOrganisationDetails());
        assertNull(out.getDefendantAccountParty().getPartyDetails().getOrganisationFlag());
        assertNull(out.getDefendantAccountParty().getAddress());
        assertNull(out.getDefendantAccountParty().getContactDetails());
        assertNull(out.getDefendantAccountParty().getVehicleDetails());
        assertNull(out.getDefendantAccountParty().getEmployerDetails());
        assertNull(out.getDefendantAccountParty().getLanguagePreferences());
    }

    @Test
    void replaceDefendantAccountParty_legacyFailure5xx_logsAndMaps() {
        LegacyReplaceDefendantAccountPartyResponse legacyBody = LegacyReplaceDefendantAccountPartyResponse.builder()
            .version(2)
            .defendantAccountParty(
                DefendantAccountPartyLegacy.builder()
                    .defendantAccountPartyType("Defendant")
                    .isDebtor(true)
                    .partyDetails(
                        PartyDetailsLegacy.builder()
                            .partyId("300")
                            .organisationFlag(true)
                            .organisationDetails(null)
                            .build()
                    )
                    .build()
            )
            .build();

        GatewayService.Response<LegacyReplaceDefendantAccountPartyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacyBody, null, null);

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.REPLACE_DEFENDANT_ACCOUNT_PARTY),
            eq(LegacyReplaceDefendantAccountPartyResponse.class),
            any(LegacyReplaceDefendantAccountPartyRequest.class),
            Mockito.nullable(String.class)
        );

        GetDefendantAccountPartyResponse out = legacyDefendantAccountService.replaceDefendantAccountParty(
            77L, 20010L, null, "1", "78", "poster", "dev_user"
        );

        assertNotNull(out);
        assertEquals(BigInteger.valueOf(2), out.getVersion());
        assertNotNull(out.getDefendantAccountParty());
        assertEquals("Defendant", out.getDefendantAccountParty().getDefendantAccountPartyType());
        assertEquals(true, out.getDefendantAccountParty().getIsDebtor());
        assertNotNull(out.getDefendantAccountParty().getPartyDetails());
        assertEquals("300", out.getDefendantAccountParty().getPartyDetails().getPartyId());
    }

    @Test
    void replaceDefendantAccountParty_exceptionBranch_rethrows() {
        doThrow(new RuntimeException("boom")).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.REPLACE_DEFENDANT_ACCOUNT_PARTY),
            eq(LegacyReplaceDefendantAccountPartyResponse.class),
            any(LegacyReplaceDefendantAccountPartyRequest.class),
            Mockito.nullable(String.class)
        );

        assertThrows(RuntimeException.class, () ->
            legacyDefendantAccountService.replaceDefendantAccountParty(77L, 20010L, null, "1", "78", "poster",
                "dev_user")
        );
    }

    @Test
    void replaceDefendantAccountParty_mapsOrganisationDetails_andIndividualIsNull() {
        LegacyReplaceDefendantAccountPartyResponse legacyBody = LegacyReplaceDefendantAccountPartyResponse.builder()
            .version(2)
            .defendantAccountParty(
                DefendantAccountPartyLegacy.builder()
                    .defendantAccountPartyType("Defendant")
                    .isDebtor(true)
                    .partyDetails(
                        PartyDetailsLegacy.builder()
                            .partyId("300")
                            .organisationFlag(true)
                            .organisationDetails(
                                uk.gov.hmcts.opal.dto.legacy.OrganisationDetailsLegacy.builder()
                                    .organisationName("StillCo Ltd")
                                    .build()
                            )
                            .individualDetails(null)
                            .build()
                    )
                    .build()
            )
            .build();

        GatewayService.Response<LegacyReplaceDefendantAccountPartyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacyBody, null, null);

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.REPLACE_DEFENDANT_ACCOUNT_PARTY),
            eq(LegacyReplaceDefendantAccountPartyResponse.class),
            any(LegacyReplaceDefendantAccountPartyRequest.class),
            Mockito.nullable(String.class)
        );

        GetDefendantAccountPartyResponse out = legacyDefendantAccountService.replaceDefendantAccountParty(
            77L, 20010L, null, "1", "78", "poster", "dev_user"
        );

        assertNotNull(out);
        assertEquals(BigInteger.valueOf(2), out.getVersion());
        assertNotNull(out.getDefendantAccountParty());
        assertEquals("Defendant", out.getDefendantAccountParty().getDefendantAccountPartyType());
        assertNotNull(out.getDefendantAccountParty().getPartyDetails());
        assertEquals("300", out.getDefendantAccountParty().getPartyDetails().getPartyId());
        assertNotNull(out.getDefendantAccountParty().getPartyDetails().getOrganisationDetails());
        assertEquals("StillCo Ltd",
            out.getDefendantAccountParty().getPartyDetails().getOrganisationDetails().getOrganisationName());
        assertNull(out.getDefendantAccountParty().getPartyDetails().getIndividualDetails());
    }

    @Test
    void replaceDefendantAccountParty_mapsIndividualDetails_andOrganisationIsNull() {
        LegacyReplaceDefendantAccountPartyResponse legacyBody = LegacyReplaceDefendantAccountPartyResponse.builder()
            .version(2)
            .defendantAccountParty(
                DefendantAccountPartyLegacy.builder()
                    .defendantAccountPartyType("Defendant")
                    .isDebtor(true)
                    .partyDetails(
                        PartyDetailsLegacy.builder()
                            .partyId("301")
                            .organisationFlag(false)
                            .organisationDetails(null)
                            .individualDetails(
                                uk.gov.hmcts.opal.dto.legacy.IndividualDetailsLegacy.builder()
                                    .title("Ms")
                                    .forenames("Jane")
                                    .surname("Roe")
                                    .dateOfBirth("1990-05-10")
                                    .age("35")
                                    .nationalInsuranceNumber("AB123456C")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build();

        GatewayService.Response<LegacyReplaceDefendantAccountPartyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacyBody, null, null);

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.REPLACE_DEFENDANT_ACCOUNT_PARTY),
            eq(LegacyReplaceDefendantAccountPartyResponse.class),
            any(LegacyReplaceDefendantAccountPartyRequest.class),
            Mockito.nullable(String.class)
        );

        GetDefendantAccountPartyResponse out = legacyDefendantAccountService.replaceDefendantAccountParty(
            77L, 20010L, null, "1", "78", "poster", "dev_user"
        );

        assertNotNull(out);
        assertEquals(BigInteger.valueOf(2), out.getVersion());
        assertNotNull(out.getDefendantAccountParty());
        assertEquals("Defendant", out.getDefendantAccountParty().getDefendantAccountPartyType());
        assertNotNull(out.getDefendantAccountParty().getPartyDetails());
        assertEquals("301", out.getDefendantAccountParty().getPartyDetails().getPartyId());
        assertNotNull(out.getDefendantAccountParty().getPartyDetails().getIndividualDetails());
        assertEquals("Ms", out.getDefendantAccountParty().getPartyDetails().getIndividualDetails().getTitle());
        assertEquals("Jane", out.getDefendantAccountParty().getPartyDetails().getIndividualDetails().getForenames());
        assertEquals("Roe", out.getDefendantAccountParty().getPartyDetails().getIndividualDetails().getSurname());
        assertNull(out.getDefendantAccountParty().getPartyDetails().getOrganisationDetails());
    }

    @Test
    void replaceDefendantAccountParty_mapsEmployerDetails_andEmployerAddress() {
        LegacyReplaceDefendantAccountPartyResponse legacyBody = LegacyReplaceDefendantAccountPartyResponse.builder()
            .version(2)
            .defendantAccountParty(
                DefendantAccountPartyLegacy.builder()
                    .defendantAccountPartyType("Defendant")
                    .isDebtor(true)
                    .partyDetails(
                        PartyDetailsLegacy.builder()
                            .partyId("400")
                            .organisationFlag(false)
                            .organisationDetails(null)
                            .individualDetails(null)
                            .build()
                    )
                    .employerDetails(
                        EmployerDetailsLegacy.builder()
                            .employerName("Acme Ltd")
                            .employerReference("REF-ACME")
                            .employerEmailAddress("hr@acme.example")
                            .employerTelephoneNumber("02071234567")
                            .employerAddress(
                                AddressDetailsLegacy.builder()
                                    .addressLine1("Acme HQ")
                                    .addressLine2("Floor 1")
                                    .addressLine3(null)
                                    .addressLine4(null)
                                    .addressLine5(null)
                                    .postcode("AC1 2CD")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build();

        GatewayService.Response<LegacyReplaceDefendantAccountPartyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacyBody, null, null);

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.REPLACE_DEFENDANT_ACCOUNT_PARTY),
            eq(LegacyReplaceDefendantAccountPartyResponse.class),
            any(LegacyReplaceDefendantAccountPartyRequest.class),
            Mockito.nullable(String.class)
        );

        GetDefendantAccountPartyResponse out = legacyDefendantAccountService.replaceDefendantAccountParty(
            77L, 20010L, null, "1", "78", "poster", "dev_user"
        );

        assertNotNull(out);
        assertEquals(BigInteger.valueOf(2), out.getVersion());
        assertNotNull(out.getDefendantAccountParty());
        assertEquals("Defendant", out.getDefendantAccountParty().getDefendantAccountPartyType());
        assertNotNull(out.getDefendantAccountParty().getEmployerDetails());
        EmployerDetails emp = out.getDefendantAccountParty().getEmployerDetails();
        assertEquals("Acme Ltd", emp.getEmployerName());
        assertEquals("REF-ACME", emp.getEmployerReference());
        assertEquals("hr@acme.example", emp.getEmployerEmailAddress());
        assertEquals("02071234567", emp.getEmployerTelephoneNumber());
        assertNotNull(emp.getEmployerAddress());
        assertEquals("Acme HQ", emp.getEmployerAddress().getAddressLine1());
        assertEquals("Floor 1", emp.getEmployerAddress().getAddressLine2());
        assertEquals("AC1 2CD", emp.getEmployerAddress().getPostcode());
    }

    @Test
    void replaceDefendantAccountParty_mapsNullEmployerDetails_toNull() {
        LegacyReplaceDefendantAccountPartyResponse legacyBody = LegacyReplaceDefendantAccountPartyResponse.builder()
            .version(2)
            .defendantAccountParty(
                DefendantAccountPartyLegacy.builder()
                    .defendantAccountPartyType("Defendant")
                    .isDebtor(false)
                    .partyDetails(
                        PartyDetailsLegacy.builder()
                            .partyId("401")
                            .organisationFlag(false)
                            .organisationDetails(null)
                            .individualDetails(null)
                            .build()
                    )
                    .employerDetails(null)
                    .build()
            )
            .build();

        GatewayService.Response<LegacyReplaceDefendantAccountPartyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacyBody, null, null);

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.REPLACE_DEFENDANT_ACCOUNT_PARTY),
            eq(LegacyReplaceDefendantAccountPartyResponse.class),
            any(LegacyReplaceDefendantAccountPartyRequest.class),
            Mockito.nullable(String.class)
        );

        GetDefendantAccountPartyResponse out = legacyDefendantAccountService.replaceDefendantAccountParty(
            77L, 20010L, null, "1", "78", "poster", "dev_user"
        );

        assertNotNull(out);
        assertEquals(BigInteger.valueOf(2), out.getVersion());
        assertNotNull(out.getDefendantAccountParty());
        assertNull(out.getDefendantAccountParty().getEmployerDetails());
    }

    @Test
    void replaceDefendantAccountParty_mapsLanguagePreferences() {
        LegacyReplaceDefendantAccountPartyResponse legacyBody = LegacyReplaceDefendantAccountPartyResponse.builder()
            .version(5)
            .defendantAccountParty(
                DefendantAccountPartyLegacy.builder()
                    .defendantAccountPartyType("Defendant")
                    .isDebtor(true)
                    .partyDetails(
                        PartyDetailsLegacy.builder()
                            .partyId("500")
                            .organisationFlag(false)
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

        GatewayService.Response<LegacyReplaceDefendantAccountPartyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacyBody, null, null);

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.REPLACE_DEFENDANT_ACCOUNT_PARTY),
            eq(LegacyReplaceDefendantAccountPartyResponse.class),
            any(LegacyReplaceDefendantAccountPartyRequest.class),
            Mockito.nullable(String.class)
        );

        GetDefendantAccountPartyResponse out = legacyDefendantAccountService.replaceDefendantAccountParty(
            77L, 20010L, null, "1", "78", "poster", "dev_user"
        );

        assertNotNull(out);
        assertEquals(BigInteger.valueOf(5), out.getVersion());
        assertNotNull(out.getDefendantAccountParty());
        assertNotNull(out.getDefendantAccountParty().getLanguagePreferences());
        assertEquals("EN",
            out.getDefendantAccountParty().getLanguagePreferences().getDocumentLanguagePreference().getLanguageCode());
    }

    @Test
    void replaceDefendantAccountParty_mapsNullLanguagePreferences_toNull() {
        LegacyReplaceDefendantAccountPartyResponse legacyBody = LegacyReplaceDefendantAccountPartyResponse.builder()
            .version(6)
            .defendantAccountParty(
                DefendantAccountPartyLegacy.builder()
                    .defendantAccountPartyType("Defendant")
                    .isDebtor(false)
                    .partyDetails(
                        PartyDetailsLegacy.builder()
                            .partyId("501")
                            .organisationFlag(false)
                            .organisationDetails(null)
                            .individualDetails(null)
                            .build()
                    )
                    .languagePreferences(null)
                    .build()
            )
            .build();

        GatewayService.Response<LegacyReplaceDefendantAccountPartyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacyBody, null, null);

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.REPLACE_DEFENDANT_ACCOUNT_PARTY),
            eq(LegacyReplaceDefendantAccountPartyResponse.class),
            any(LegacyReplaceDefendantAccountPartyRequest.class),
            Mockito.nullable(String.class)
        );

        GetDefendantAccountPartyResponse out = legacyDefendantAccountService.replaceDefendantAccountParty(
            77L, 20010L, null, "1", "78", "poster", "dev_user"
        );

        assertNotNull(out);
        assertEquals(BigInteger.valueOf(6), out.getVersion());
        assertNotNull(out.getDefendantAccountParty());
        assertNull(out.getDefendantAccountParty().getLanguagePreferences());
    }
}
