package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.ContactDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountPartyLegacy;
import uk.gov.hmcts.opal.dto.legacy.EmployerDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.IndividualDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.OrganisationDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.VehicleDetailsLegacy;

class LegacyDefAccServiceDefAccPartyTest extends AbstractLegacyDefAccServiceTest {

    private GetDefendantAccountPartyLegacyResponse legacyPartyIndividual() {
        IndividualDetailsLegacy ind = IndividualDetailsLegacy.builder()
            .title("Ms").forenames("Sam").surname("Graham").build();

        PartyDetailsLegacy pd = PartyDetailsLegacy.builder()
            .partyId("77").organisationFlag(false).individualDetails(ind).build();

        DefendantAccountPartyLegacy party = DefendantAccountPartyLegacy.builder()
            .defendantAccountPartyType("Defendant")
            .isDebtor(true)
            .partyDetails(pd)
            // leave contact & vehicle empty so mapper drops them
            .contactDetails(ContactDetailsLegacy.builder().build())
            .vehicleDetails(VehicleDetailsLegacy.builder().build())
            .build();

        return GetDefendantAccountPartyLegacyResponse.builder()
            .version(1L)
            .defendantAccountParty(party)
            .build();
    }

    private GetDefendantAccountPartyLegacyResponse legacyPartyOrganisation(boolean withEmpLine1) {
        OrganisationDetailsLegacy org = OrganisationDetailsLegacy.builder()
            .organisationName("TechCorp Solutions Ltd").build();

        PartyDetailsLegacy pd = PartyDetailsLegacy.builder()
            .partyId("555").organisationFlag(true).organisationDetails(org).build();

        AddressDetailsLegacy empAddr = withEmpLine1
            ? AddressDetailsLegacy.builder().addressLine1("1 High St").postcode("AB1 2CD").build()
            : AddressDetailsLegacy.builder().postcode("AB1 2CD").build();

        EmployerDetailsLegacy emp = EmployerDetailsLegacy.builder()
            .employerName("Widgets Ltd")
            .employerAddress(empAddr)
            .build();

        DefendantAccountPartyLegacy party = DefendantAccountPartyLegacy.builder()
            .defendantAccountPartyType("Defendant")
            .isDebtor(true)
            .partyDetails(pd)
            .employerDetails(emp)
            .build();

        return GetDefendantAccountPartyLegacyResponse.builder()
            .version(2L)
            .defendantAccountParty(party)
            .build();
    }

    @Test
    void getDefendantAccountParty_success_individual_dropsEmptySections() {
        // arrange
        GetDefendantAccountPartyLegacyResponse legacy = legacyPartyIndividual(); // version = 1L
        GatewayService.Response<GetDefendantAccountPartyLegacyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacy, null, null);

        // help the compiler by using a typed local for the Class<T> matcher
        Class<GetDefendantAccountPartyLegacyResponse> respType =
            GetDefendantAccountPartyLegacyResponse.class;

        // act: stub the spy’d gateway to return our Response
        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.GET_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            any(GetDefendantAccountPartyLegacyRequest.class),
            Mockito.nullable(String.class)
        );

        // assert
        GetDefendantAccountPartyResponse out =
            legacyDefendantAccountService.getDefendantAccountParty(77L, 77L);

        assertNotNull(out);
        assertEquals(BigInteger.valueOf(1L), out.getVersion());
        assertNotNull(out.getDefendantAccountParty());
        // individual kept, organisation null
        assertNotNull(out.getDefendantAccountParty().getPartyDetails().getIndividualDetails());
        assertNull(out.getDefendantAccountParty().getPartyDetails().getOrganisationDetails());
        // empty contact/vehicle dropped
        assertNull(out.getDefendantAccountParty().getContactDetails());
        assertNull(out.getDefendantAccountParty().getVehicleDetails());
    }

    @Test
    void getDefendantAccountParty_success_org_employerAddressOnlyWhenLine1Present() {
        // arrange: two gateway responses — first without line1 (drop), then with line1 (keep)
        GatewayService.Response<GetDefendantAccountPartyLegacyResponse> respA =
            new GatewayService.Response<>(HttpStatus.OK, legacyPartyOrganisation(false), null, null);
        GatewayService.Response<GetDefendantAccountPartyLegacyResponse> respB =
            new GatewayService.Response<>(HttpStatus.OK, legacyPartyOrganisation(true),  null, null);

        // help the compiler with a typed local for Class<T>
        Class<GetDefendantAccountPartyLegacyResponse> respType =
            GetDefendantAccountPartyLegacyResponse.class;

        // stub spy’d gateway: first call => respA, second call => respB
        doReturn(respA, respB).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.GET_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            any(GetDefendantAccountPartyLegacyRequest.class),
            Mockito.nullable(String.class)
        );

        // act/assert 1) address_line_1 missing -> employerAddress dropped
        GetDefendantAccountPartyResponse outA =
            legacyDefendantAccountService.getDefendantAccountParty(555L, 555L);
        assertNotNull(outA);
        assertEquals(BigInteger.valueOf(2L), outA.getVersion());
        assertNotNull(outA.getDefendantAccountParty());
        assertNotNull(outA.getDefendantAccountParty().getPartyDetails().getOrganisationDetails());
        assertNull(outA.getDefendantAccountParty().getPartyDetails().getIndividualDetails());
        assertNotNull(outA.getDefendantAccountParty().getEmployerDetails());
        assertNull(outA.getDefendantAccountParty().getEmployerDetails().getEmployerAddress());

        // act/assert 2) address_line_1 present -> employerAddress kept
        GetDefendantAccountPartyResponse outB =
            legacyDefendantAccountService.getDefendantAccountParty(555L, 555L);
        assertNotNull(outB);
        assertEquals(BigInteger.valueOf(2L), outB.getVersion());
        assertNotNull(outB.getDefendantAccountParty());
        assertNotNull(outB.getDefendantAccountParty().getPartyDetails().getOrganisationDetails());
        assertNull(outB.getDefendantAccountParty().getPartyDetails().getIndividualDetails());
        uk.gov.hmcts.opal.dto.common.AddressDetails kept =
            outB.getDefendantAccountParty().getEmployerDetails().getEmployerAddress();
        assertNotNull(kept);
        assertEquals("1 High St", kept.getAddressLine1());
        assertEquals("AB1 2CD", kept.getPostcode());
    }

    @Test
    void getDefendantAccountParty_legacyFailure5xx_withEntity_partyNull() {
        // arrange
        GetDefendantAccountPartyLegacyResponse legacy =
            GetDefendantAccountPartyLegacyResponse.builder()
                .version(99L)
                .defendantAccountParty(null)
                .build();

        GatewayService.Response<GetDefendantAccountPartyLegacyResponse> resp =
            new GatewayService.Response<>(HttpStatus.SERVICE_UNAVAILABLE, legacy, "<legacy-failure/>", null);

        Class<GetDefendantAccountPartyLegacyResponse> respType =
            GetDefendantAccountPartyLegacyResponse.class;

        // stub
        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.GET_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            any(GetDefendantAccountPartyLegacyRequest.class),
            Mockito.nullable(String.class)
        );

        // act
        GetDefendantAccountPartyResponse out =
            legacyDefendantAccountService.getDefendantAccountParty(11L, 22L);

        // assert
        assertNotNull(out);
        assertNull(out.getVersion());
        assertNull(out.getDefendantAccountParty());
    }

    @Test
    void getDefendantAccountParty_error_exceptionBranch_returnsWrapperWithNulls() {
        // arrange: exception path (no entity)
        GatewayService.Response<GetDefendantAccountPartyLegacyResponse> resp =
            new GatewayService.Response<>(HttpStatus.BAD_GATEWAY, new RuntimeException("boom"), null);

        Class<GetDefendantAccountPartyLegacyResponse> respType =
            GetDefendantAccountPartyLegacyResponse.class;

        // stub the spy’d gateway to hit the (String, Class<T>, Object, String) overload
        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.GET_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            any(GetDefendantAccountPartyLegacyRequest.class),
            Mockito.nullable(String.class)
        );

        // act
        GetDefendantAccountPartyResponse out =
            legacyDefendantAccountService.getDefendantAccountParty(12L, 23L);

        // assert
        assertNotNull(out);
        assertNull(out.getVersion());
        assertNull(out.getDefendantAccountParty());
    }

    @Test
    void shouldReturnDefendantAccountPartyWhenGatewayResponseIsSuccessful() {
        // Arrange
        Long defendantAccountId = 123L;
        Long defendantAccountPartyId = 456L;

        // Use a real legacy entity so mapping has data
        GetDefendantAccountPartyLegacyResponse legacy = legacyPartyIndividual(); // e.g. version=1L, individual set
        GatewayService.Response<GetDefendantAccountPartyLegacyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacy, null, null);

        Class<GetDefendantAccountPartyLegacyResponse> respType = GetDefendantAccountPartyLegacyResponse.class;

        // Stub spy’d gateway (choose correct overload)
        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.GET_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            any(GetDefendantAccountPartyLegacyRequest.class),
            Mockito.nullable(String.class)
        );

        // Act
        GetDefendantAccountPartyResponse result =
            legacyDefendantAccountService.getDefendantAccountParty(defendantAccountId, defendantAccountPartyId);

        // Assert: non-null and some basic fields
        assertNotNull(result);
        assertEquals(BigInteger.valueOf(1L), result.getVersion());
        assertNotNull(result.getDefendantAccountParty());

        // Verify gateway called once with properly stringified IDs in the request
        ArgumentCaptor<GetDefendantAccountPartyLegacyRequest> reqCap =
            ArgumentCaptor.forClass(GetDefendantAccountPartyLegacyRequest.class);

        verify(gatewayService, times(1)).postToGateway(
            eq(LegacyDefendantAccountService.GET_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            reqCap.capture(),
            Mockito.nullable(String.class)
        );

        GetDefendantAccountPartyLegacyRequest sent = reqCap.getValue();
        assertEquals("123", sent.getDefendantAccountId());
        assertEquals("456", sent.getDefendantAccountPartyId());
    }

    @Test
    void getDefendantAccountParty_clientError_withEntity_partyNull() {
        // arrange
        GetDefendantAccountPartyLegacyResponse legacy =
            GetDefendantAccountPartyLegacyResponse.builder()
                .version(42L)
                .defendantAccountParty(null)
                .build();

        GatewayService.Response<GetDefendantAccountPartyLegacyResponse> resp =
            new GatewayService.Response<>(HttpStatus.BAD_REQUEST, legacy, "<bad-request/>", null);

        Class<GetDefendantAccountPartyLegacyResponse> respType =
            GetDefendantAccountPartyLegacyResponse.class;

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.GET_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            any(GetDefendantAccountPartyLegacyRequest.class),
            Mockito.nullable(String.class)
        );

        // act
        GetDefendantAccountPartyResponse out =
            legacyDefendantAccountService.getDefendantAccountParty(123L, 456L);

        // assert
        assertNotNull(out);
        assertNull(out.getVersion());
        assertNull(out.getDefendantAccountParty());

        verify(gatewayService, times(1)).postToGateway(
            eq(LegacyDefendantAccountService.GET_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            any(GetDefendantAccountPartyLegacyRequest.class),
            Mockito.nullable(String.class)
        );
    }

    @Test
    void shouldCreateCorrectRequestForGateway() {
        // arrange
        Long defendantAccountId = 123L;
        Long defendantAccountPartyId = 456L;

        ArgumentCaptor<GetDefendantAccountPartyLegacyRequest> requestCaptor =
            ArgumentCaptor.forClass(GetDefendantAccountPartyLegacyRequest.class);

        Class<GetDefendantAccountPartyLegacyResponse> respType =
            GetDefendantAccountPartyLegacyResponse.class;

        GatewayService.Response<GetDefendantAccountPartyLegacyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, null, null, null);

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.GET_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            any(GetDefendantAccountPartyLegacyRequest.class),
            Mockito.nullable(String.class)
        );

        // act
        legacyDefendantAccountService.getDefendantAccountParty(defendantAccountId, defendantAccountPartyId);

        // assert
        verify(gatewayService, times(1)).postToGateway(
            eq(LegacyDefendantAccountService.GET_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            requestCaptor.capture(),
            Mockito.nullable(String.class)
        );

        GetDefendantAccountPartyLegacyRequest captured = requestCaptor.getValue();
        assertEquals(defendantAccountId.toString(), captured.getDefendantAccountId());
        assertEquals(defendantAccountPartyId.toString(), captured.getDefendantAccountPartyId());
    }

    @Test
    void getDefendantAccountParty_contact_kept_whenPrimaryEmailPresent() {
        ContactDetailsLegacy contact = ContactDetailsLegacy.builder()
            .primaryEmailAddress("sam@example.com")
            .build();

        PartyDetailsLegacy pd = PartyDetailsLegacy.builder()
            .partyId("77").organisationFlag(false)
            .individualDetails(IndividualDetailsLegacy.builder().forenames("Sam").surname("Graham").build())
            .build();

        DefendantAccountPartyLegacy party = DefendantAccountPartyLegacy.builder()
            .partyDetails(pd)
            .contactDetails(contact)
            .build();

        GetDefendantAccountPartyLegacyResponse legacy =
            GetDefendantAccountPartyLegacyResponse.builder().version(1L).defendantAccountParty(party).build();

        GatewayService.Response<GetDefendantAccountPartyLegacyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacy, null, null);

        Class<GetDefendantAccountPartyLegacyResponse> respType = GetDefendantAccountPartyLegacyResponse.class;

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.GET_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            any(GetDefendantAccountPartyLegacyRequest.class),
            Mockito.nullable(String.class)
        );

        GetDefendantAccountPartyResponse out =
            legacyDefendantAccountService.getDefendantAccountParty(77L, 77L);

        assertNotNull(out.getDefendantAccountParty().getContactDetails());
        assertEquals("sam@example.com",
            out.getDefendantAccountParty().getContactDetails().getPrimaryEmailAddress());
    }

    @Test
    void getDefendantAccountParty_employer_dropped_whenAllFieldsNull() {
        PartyDetailsLegacy pd = PartyDetailsLegacy.builder()
            .partyId("555").organisationFlag(true)
            .organisationDetails(OrganisationDetailsLegacy.builder().organisationName("TechCorp").build())
            .build();

        EmployerDetailsLegacy emptyEmp = EmployerDetailsLegacy.builder().build();

        DefendantAccountPartyLegacy party = DefendantAccountPartyLegacy.builder()
            .partyDetails(pd)
            .employerDetails(emptyEmp)
            .build();

        GetDefendantAccountPartyLegacyResponse legacy =
            GetDefendantAccountPartyLegacyResponse.builder().version(2L).defendantAccountParty(party).build();

        GatewayService.Response<GetDefendantAccountPartyLegacyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacy, null, null);

        Class<GetDefendantAccountPartyLegacyResponse> respType = GetDefendantAccountPartyLegacyResponse.class;

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.GET_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            any(GetDefendantAccountPartyLegacyRequest.class),
            Mockito.nullable(String.class)
        );

        GetDefendantAccountPartyResponse out =
            legacyDefendantAccountService.getDefendantAccountParty(555L, 555L);

        assertNull(out.getDefendantAccountParty().getEmployerDetails());
    }

    @Test
    void getDefendantAccountParty_address_maps_all_present_lines() {
        AddressDetailsLegacy addr = AddressDetailsLegacy.builder()
            .addressLine1("1 High St")
            .addressLine2("Suite 5")
            .addressLine3("District")
            .addressLine4("County")
            .addressLine5("Country")
            .postcode("AB1 2CD")
            .build();

        PartyDetailsLegacy pd = PartyDetailsLegacy.builder()
            .partyId("77").organisationFlag(false)
            .individualDetails(IndividualDetailsLegacy.builder().forenames("Sam").surname("Graham").build())
            .build();

        DefendantAccountPartyLegacy party = DefendantAccountPartyLegacy.builder()
            .partyDetails(pd)
            .address(addr)
            .build();

        GetDefendantAccountPartyLegacyResponse legacy =
            GetDefendantAccountPartyLegacyResponse.builder().version(1L).defendantAccountParty(party).build();

        GatewayService.Response<GetDefendantAccountPartyLegacyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacy, null, null);

        Class<GetDefendantAccountPartyLegacyResponse> respType = GetDefendantAccountPartyLegacyResponse.class;

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.GET_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            any(GetDefendantAccountPartyLegacyRequest.class),
            Mockito.nullable(String.class)
        );

        GetDefendantAccountPartyResponse out =
            legacyDefendantAccountService.getDefendantAccountParty(77L, 77L);

        uk.gov.hmcts.opal.dto.common.AddressDetails mapped = out.getDefendantAccountParty().getAddress();
        assertNotNull(mapped);
        assertEquals("1 High St", mapped.getAddressLine1());
        assertEquals("Suite 5",  mapped.getAddressLine2());
        assertEquals("District", mapped.getAddressLine3());
        assertEquals("County",   mapped.getAddressLine4());
        assertEquals("Country",  mapped.getAddressLine5());
        assertEquals("AB1 2CD",  mapped.getPostcode());
    }

    @Test
    void getDefendantAccountParty_languagePreferences_buildsContainer_whenCodesPresent() {
        uk.gov.hmcts.opal.dto.legacy.LanguagePreferencesLegacy.LanguagePreference doc =
            uk.gov.hmcts.opal.dto.legacy.LanguagePreferencesLegacy.LanguagePreference.builder()
            .languageCode("EN").build();
        uk.gov.hmcts.opal.dto.legacy.LanguagePreferencesLegacy.LanguagePreference hear =
            uk.gov.hmcts.opal.dto.legacy.LanguagePreferencesLegacy.LanguagePreference.builder()
            .languageCode("CY").build();

        uk.gov.hmcts.opal.dto.legacy.LanguagePreferencesLegacy langs =
            uk.gov.hmcts.opal.dto.legacy.LanguagePreferencesLegacy.builder()
            .documentLanguagePreference(doc)
            .hearingLanguagePreference(hear)
            .build();

        uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy pd =
            uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy.builder()
            .partyId("77").organisationFlag(false)
            .individualDetails(uk.gov.hmcts.opal.dto.legacy.IndividualDetailsLegacy.builder()
                .forenames("Sam").surname("Graham").build())
            .build();

        uk.gov.hmcts.opal.dto.legacy.DefendantAccountPartyLegacy party =
            uk.gov.hmcts.opal.dto.legacy.DefendantAccountPartyLegacy.builder()
            .partyDetails(pd)
            .languagePreferences(langs)
            .build();

        uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponse legacy =
            uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponse.builder()
            .version(1L).defendantAccountParty(party).build();

        uk.gov.hmcts.opal.service.legacy.GatewayService.Response<uk.gov.hmcts.opal.dto.legacy
            .GetDefendantAccountPartyLegacyResponse> resp =
            new uk.gov.hmcts.opal.service.legacy.GatewayService.Response<>(
                org.springframework.http.HttpStatus.OK, legacy, null, null);

        Class<uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponse> respType =
            uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponse.class;

        org.mockito.Mockito.doReturn(resp).when(gatewayService).postToGateway(
            org.mockito.ArgumentMatchers.eq(uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountService
                .GET_DEFENDANT_ACCOUNT_PARTY),
            org.mockito.ArgumentMatchers.eq(respType),
            org.mockito.ArgumentMatchers.any(uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyRequest.class),
            org.mockito.Mockito.nullable(String.class)
        );

        GetDefendantAccountPartyResponse out = legacyDefendantAccountService.getDefendantAccountParty(77L, 77L);
        uk.gov.hmcts.opal.dto.common.LanguagePreferences prefs =
            out.getDefendantAccountParty().getLanguagePreferences();

        org.junit.jupiter.api.Assertions.assertNotNull(prefs);
        org.junit.jupiter.api.Assertions.assertEquals("EN",
            prefs.getDocumentLanguagePreference().getLanguageCode());
        org.junit.jupiter.api.Assertions.assertEquals("CY",
            prefs.getHearingLanguagePreference().getLanguageCode());
    }
}
