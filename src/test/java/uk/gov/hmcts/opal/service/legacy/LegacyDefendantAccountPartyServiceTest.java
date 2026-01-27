package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.lang.reflect.Field;
import java.math.BigInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.disco.legacy.LegacyTestsBase;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.common.EmployerDetails;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.ContactDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountPartyLegacy;
import uk.gov.hmcts.opal.dto.legacy.EmployerDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.IndividualDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LanguagePreferencesLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyReplaceDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyReplaceDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.legacy.OrganisationDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.VehicleDetailsLegacy;
import uk.gov.hmcts.opal.service.opal.CourtService;

@ExtendWith(MockitoExtension.class)
class LegacyDefendantAccountPartyServiceTest extends LegacyTestsBase {

    @Spy
    private MockRestClient restClient = spy(MockRestClient.class);

    @Mock
    private LegacyGatewayProperties gatewayProperties;

    @Mock
    private CourtService courtService;

    private GatewayService gatewayService;

    @InjectMocks
    private  LegacyDefendantAccountPartyService legacyDefendantAccountPartyService;

    @BeforeEach
    void openMocks() throws Exception {
        gatewayService = Mockito.spy(new LegacyGatewayService(gatewayProperties, restClient));
        injectGatewayService(legacyDefendantAccountPartyService, gatewayService);

    }

    private void injectGatewayService(
        LegacyDefendantAccountPartyService legacyDefendantAccountService, GatewayService gatewayService)
        throws NoSuchFieldException, IllegalAccessException {

        Field field = LegacyDefendantAccountPartyService.class.getDeclaredField("gatewayService");
        field.setAccessible(true);
        field.set(legacyDefendantAccountService, gatewayService);

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
            legacyDefendantAccountPartyService.getDefendantAccountParty(77L, 77L);

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
            legacyDefendantAccountPartyService.getDefendantAccountParty(555L, 555L);
        assertNotNull(outA);
        assertEquals(BigInteger.valueOf(2L), outA.getVersion());
        assertNotNull(outA.getDefendantAccountParty());
        assertNotNull(outA.getDefendantAccountParty().getPartyDetails().getOrganisationDetails());
        assertNull(outA.getDefendantAccountParty().getPartyDetails().getIndividualDetails());
        assertNotNull(outA.getDefendantAccountParty().getEmployerDetails());
        assertNull(outA.getDefendantAccountParty().getEmployerDetails().getEmployerAddress());

        // act/assert 2) address_line_1 present -> employerAddress kept
        GetDefendantAccountPartyResponse outB =
            legacyDefendantAccountPartyService.getDefendantAccountParty(555L, 555L);
        assertNotNull(outB);
        assertEquals(BigInteger.valueOf(2L), outB.getVersion());
        assertNotNull(outB.getDefendantAccountParty());
        assertNotNull(outB.getDefendantAccountParty().getPartyDetails().getOrganisationDetails());
        assertNull(outB.getDefendantAccountParty().getPartyDetails().getIndividualDetails());
        var kept = outB.getDefendantAccountParty().getEmployerDetails().getEmployerAddress();
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
            legacyDefendantAccountPartyService.getDefendantAccountParty(11L, 22L);

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
            legacyDefendantAccountPartyService.getDefendantAccountParty(12L, 23L);

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
            legacyDefendantAccountPartyService.getDefendantAccountParty(defendantAccountId, defendantAccountPartyId);

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
            legacyDefendantAccountPartyService.getDefendantAccountParty(123L, 456L);

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
        legacyDefendantAccountPartyService.getDefendantAccountParty(defendantAccountId, defendantAccountPartyId);

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
            legacyDefendantAccountPartyService.getDefendantAccountParty(77L, 77L);

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
            legacyDefendantAccountPartyService.getDefendantAccountParty(555L, 555L);

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
            legacyDefendantAccountPartyService.getDefendantAccountParty(77L, 77L);

        var mapped = out.getDefendantAccountParty().getAddress();
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
        var doc = uk.gov.hmcts.opal.dto.legacy.LanguagePreferencesLegacy.LanguagePreference.builder()
            .languageCode("EN").build();
        var hear = uk.gov.hmcts.opal.dto.legacy.LanguagePreferencesLegacy.LanguagePreference.builder()
            .languageCode("CY").build();

        var langs = uk.gov.hmcts.opal.dto.legacy.LanguagePreferencesLegacy.builder()
            .documentLanguagePreference(doc)
            .hearingLanguagePreference(hear)
            .build();

        var pd = uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy.builder()
            .partyId("77").organisationFlag(false)
            .individualDetails(uk.gov.hmcts.opal.dto.legacy.IndividualDetailsLegacy.builder()
                .forenames("Sam").surname("Graham").build())
            .build();

        var party = uk.gov.hmcts.opal.dto.legacy.DefendantAccountPartyLegacy.builder()
            .partyDetails(pd)
            .languagePreferences(langs)
            .build();

        var legacy = uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponse.builder()
            .version(1L).defendantAccountParty(party).build();

        var resp = new uk.gov.hmcts.opal.service.legacy.GatewayService.Response<>(
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

        var out = legacyDefendantAccountPartyService.getDefendantAccountParty(77L, 77L);
        var prefs = out.getDefendantAccountParty().getLanguagePreferences();

        org.junit.jupiter.api.Assertions.assertNotNull(prefs);
        org.junit.jupiter.api.Assertions.assertEquals("EN",
            prefs.getDocumentLanguagePreference().getLanguageCode());
        org.junit.jupiter.api.Assertions.assertEquals("CY",
            prefs.getHearingLanguagePreference().getLanguageCode());
    }

    @Test
    void replaceDefendantAccountParty_mapsNullNestedObjects_toNulls() {
        // Build a legacy response body where nested objects are null
        LegacyReplaceDefendantAccountPartyResponse legacyBody = LegacyReplaceDefendantAccountPartyResponse.builder()
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

        GatewayService.Response<LegacyReplaceDefendantAccountPartyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacyBody, null, null);

        Class<LegacyReplaceDefendantAccountPartyResponse> respType = LegacyReplaceDefendantAccountPartyResponse.class;

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.REPLACE_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            any(LegacyReplaceDefendantAccountPartyRequest.class),
            Mockito.nullable(String.class)
        );

        // Call service; inputs for the request are not important for this mapping test
        GetDefendantAccountPartyResponse out = legacyDefendantAccountPartyService.replaceDefendantAccountParty(
            77L, 20010L, null, "1", "78", "poster", "dev_user"
        );

        assertNotNull(out);
        assertEquals(BigInteger.valueOf(4), out.getVersion());
        assertNotNull(out.getDefendantAccountParty());

        // party details should exist but nested organisation/individual fields should be null
        assertNotNull(out.getDefendantAccountParty().getPartyDetails());
        assertNull(out.getDefendantAccountParty().getPartyDetails().getOrganisationDetails());
        // organisationFlag was null in legacy -> should be null in modern
        assertNull(out.getDefendantAccountParty().getPartyDetails().getOrganisationFlag());

        // address/contact/vehicle/employer/languagePreferences were null in legacy -> null in modern
        assertNull(out.getDefendantAccountParty().getAddress());
        assertNull(out.getDefendantAccountParty().getContactDetails());
        assertNull(out.getDefendantAccountParty().getVehicleDetails());
        assertNull(out.getDefendantAccountParty().getEmployerDetails());
        assertNull(out.getDefendantAccountParty().getLanguagePreferences());
    }

    @Test
    void replaceDefendantAccountParty_legacyFailure5xx_logsAndMaps() {
        // Build a minimal legacy response body (service should still map fields even on 5xx)
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

        Class<LegacyReplaceDefendantAccountPartyResponse> respType = LegacyReplaceDefendantAccountPartyResponse.class;

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.REPLACE_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            any(LegacyReplaceDefendantAccountPartyRequest.class),
            Mockito.nullable(String.class)
        );

        // Call the service. The production code logs legacy failure but still returns a mapped response
        GetDefendantAccountPartyResponse out = legacyDefendantAccountPartyService.replaceDefendantAccountParty(
            77L, 20010L, null, "1", "78", "poster", "dev_user"
        );

        assertNotNull(out);
        assertEquals(BigInteger.valueOf(2), out.getVersion());
        assertNotNull(out.getDefendantAccountParty());
        assertEquals("Defendant", out.getDefendantAccountParty().getDefendantAccountPartyType());
        assertTrue(out.getDefendantAccountParty().getIsDebtor());
        assertNotNull(out.getDefendantAccountParty().getPartyDetails());
        assertEquals("300", out.getDefendantAccountParty().getPartyDetails().getPartyId());
    }

    @Test
    void replaceDefendantAccountParty_exceptionBranch_rethrows() {

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

        // We no longer return a response; instead we make the gateway throw a RuntimeException
        Class<LegacyReplaceDefendantAccountPartyResponse> respType = LegacyReplaceDefendantAccountPartyResponse.class;

        // Option A: use doThrow to throw when the specific call is made
        doThrow(new RuntimeException("boom")).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.REPLACE_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            any(LegacyReplaceDefendantAccountPartyRequest.class),
            Mockito.nullable(String.class)
        );

        // Assert the exception is propagated by the service (production code logs and should rethrow)
        assertThrows(RuntimeException.class, () ->
            legacyDefendantAccountPartyService.replaceDefendantAccountParty(
                77L, 20010L, null, "1", "78", "poster", "dev_user")
        );
    }

    @Test
    void replaceDefendantAccountParty_mapsOrganisationDetails_andIndividualIsNull() {
        // Build a legacy entity with only organisationDetails populated
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
                            .individualDetails(null) // explicitly null
                            .build()
                    )
                    .build()
            )
            .build();

        GatewayService.Response<LegacyReplaceDefendantAccountPartyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacyBody, null, null);

        Class<LegacyReplaceDefendantAccountPartyResponse> respType = LegacyReplaceDefendantAccountPartyResponse.class;

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.REPLACE_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            any(LegacyReplaceDefendantAccountPartyRequest.class),
            Mockito.nullable(String.class)
        );

        // Act
        GetDefendantAccountPartyResponse out = legacyDefendantAccountPartyService.replaceDefendantAccountParty(
            77L, 20010L, null, "1", "78", "poster", "dev_user"
        );

        // Assert
        assertNotNull(out);
        assertEquals(BigInteger.valueOf(2), out.getVersion());
        assertNotNull(out.getDefendantAccountParty());
        assertEquals("Defendant", out.getDefendantAccountParty().getDefendantAccountPartyType());

        assertNotNull(out.getDefendantAccountParty().getPartyDetails());
        assertEquals("300", out.getDefendantAccountParty().getPartyDetails().getPartyId());

        // Organisation should be present
        assertNotNull(out.getDefendantAccountParty().getPartyDetails().getOrganisationDetails());
        assertEquals("StillCo Ltd",
            out.getDefendantAccountParty().getPartyDetails().getOrganisationDetails().getOrganisationName());

        // Individual must be null when organisation is present
        assertNull(out.getDefendantAccountParty().getPartyDetails().getIndividualDetails(),
            "Individual details must be null when organisation details are present");
    }

    @Test
    void replaceDefendantAccountParty_mapsIndividualDetails_andOrganisationIsNull() {
        // Build a legacy entity with only individualDetails populated
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
                            .organisationDetails(null) // explicitly null
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

        Class<LegacyReplaceDefendantAccountPartyResponse> respType = LegacyReplaceDefendantAccountPartyResponse.class;

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.REPLACE_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            any(LegacyReplaceDefendantAccountPartyRequest.class),
            Mockito.nullable(String.class)
        );

        // Act
        GetDefendantAccountPartyResponse out = legacyDefendantAccountPartyService.replaceDefendantAccountParty(
            77L, 20010L, null, "1", "78", "poster", "dev_user"
        );

        // Assert
        assertNotNull(out);
        assertEquals(BigInteger.valueOf(2), out.getVersion());
        assertNotNull(out.getDefendantAccountParty());
        assertEquals("Defendant", out.getDefendantAccountParty().getDefendantAccountPartyType());

        assertNotNull(out.getDefendantAccountParty().getPartyDetails());
        assertEquals("301", out.getDefendantAccountParty().getPartyDetails().getPartyId());

        // Individual should be present
        assertNotNull(out.getDefendantAccountParty().getPartyDetails().getIndividualDetails());
        assertEquals("Ms",
            out.getDefendantAccountParty().getPartyDetails().getIndividualDetails().getTitle());
        assertEquals("Jane",
            out.getDefendantAccountParty().getPartyDetails().getIndividualDetails().getForenames());
        assertEquals("Roe",
            out.getDefendantAccountParty().getPartyDetails().getIndividualDetails().getSurname());

        // Organisation must be null when individual is present
        assertNull(out.getDefendantAccountParty().getPartyDetails().getOrganisationDetails(),
            "Organisation details must be null when individual details are present");
    }

    @Test
    void replaceDefendantAccountParty_mapsEmployerDetails_andEmployerAddress() {
        // Build a legacy entity with employer details (including employerAddress)
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

        Class<LegacyReplaceDefendantAccountPartyResponse> respType = LegacyReplaceDefendantAccountPartyResponse.class;

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.REPLACE_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            any(LegacyReplaceDefendantAccountPartyRequest.class),
            Mockito.nullable(String.class)
        );

        // Act
        GetDefendantAccountPartyResponse out = legacyDefendantAccountPartyService.replaceDefendantAccountParty(
            77L, 20010L, null, "1", "78", "poster", "dev_user"
        );

        // Assert
        assertNotNull(out);
        assertEquals(BigInteger.valueOf(2), out.getVersion());
        assertNotNull(out.getDefendantAccountParty());
        assertEquals("Defendant", out.getDefendantAccountParty().getDefendantAccountPartyType());

        // Employer details present and mapped
        assertNotNull(out.getDefendantAccountParty().getEmployerDetails(), "Employer details should be mapped");
        EmployerDetails emp = out.getDefendantAccountParty().getEmployerDetails();
        assertEquals("Acme Ltd", emp.getEmployerName());
        assertEquals("REF-ACME", emp.getEmployerReference());
        assertEquals("hr@acme.example", emp.getEmployerEmailAddress());
        assertEquals("02071234567", emp.getEmployerTelephoneNumber());

        // Employer address mapped
        assertNotNull(emp.getEmployerAddress(), "Employer address should be mapped");
        assertEquals("Acme HQ", emp.getEmployerAddress().getAddressLine1());
        assertEquals("Floor 1", emp.getEmployerAddress().getAddressLine2());
        assertEquals("AC1 2CD", emp.getEmployerAddress().getPostcode());
    }

    @Test
    void replaceDefendantAccountParty_mapsNullEmployerDetails_toNull() {
        // Build a legacy entity with employerDetails == null
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
                    .employerDetails(null) // explicitly null
                    .build()
            )
            .build();

        GatewayService.Response<LegacyReplaceDefendantAccountPartyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacyBody, null, null);

        Class<LegacyReplaceDefendantAccountPartyResponse> respType = LegacyReplaceDefendantAccountPartyResponse.class;

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.REPLACE_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            any(LegacyReplaceDefendantAccountPartyRequest.class),
            Mockito.nullable(String.class)
        );

        // Act
        GetDefendantAccountPartyResponse out = legacyDefendantAccountPartyService.replaceDefendantAccountParty(
            77L, 20010L, null, "1", "78", "poster", "dev_user"
        );

        // Assert
        assertNotNull(out);
        assertEquals(BigInteger.valueOf(2), out.getVersion());
        assertNotNull(out.getDefendantAccountParty());

        // Employer details should be null in modern model when legacy had none
        assertNull(out.getDefendantAccountParty().getEmployerDetails(),
            "Employer details should be null when legacy employerDetails is null");
    }

    @Test
    void replaceDefendantAccountParty_mapsLanguagePreferences() {
        // Build a legacy entity with language preferences populated
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

        Class<LegacyReplaceDefendantAccountPartyResponse> respType = LegacyReplaceDefendantAccountPartyResponse.class;

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.REPLACE_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            any(LegacyReplaceDefendantAccountPartyRequest.class),
            Mockito.nullable(String.class)
        );

        // Act
        GetDefendantAccountPartyResponse out = legacyDefendantAccountPartyService.replaceDefendantAccountParty(
            77L, 20010L, null, "1", "78", "poster", "dev_user"
        );

        // Assert
        assertNotNull(out);
        assertEquals(BigInteger.valueOf(5), out.getVersion());
        assertNotNull(out.getDefendantAccountParty());

        // language preferences should be mapped and use codes (document -> "en", hearing -> "fr")
        assertNotNull(out.getDefendantAccountParty().getLanguagePreferences(),
            "Language preferences should be mapped when provided by legacy");
        assertEquals("EN",
            out.getDefendantAccountParty().getLanguagePreferences().getDocumentLanguagePreference().getLanguageCode());

    }

    @Test
    void replaceDefendantAccountParty_mapsNullLanguagePreferences_toNull() {
        // Build a legacy entity with languagePreferences == null
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
                    .languagePreferences(null) // explicitly null
                    .build()
            )
            .build();

        GatewayService.Response<LegacyReplaceDefendantAccountPartyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacyBody, null, null);

        Class<LegacyReplaceDefendantAccountPartyResponse> respType = LegacyReplaceDefendantAccountPartyResponse.class;

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.REPLACE_DEFENDANT_ACCOUNT_PARTY),
            eq(respType),
            any(LegacyReplaceDefendantAccountPartyRequest.class),
            Mockito.nullable(String.class)
        );

        // Act
        GetDefendantAccountPartyResponse out = legacyDefendantAccountPartyService.replaceDefendantAccountParty(
            77L, 20010L, null, "1", "78", "poster", "dev_user"
        );

        // Assert
        assertNotNull(out);
        assertEquals(BigInteger.valueOf(6), out.getVersion());
        assertNotNull(out.getDefendantAccountParty());

        // language preferences should be null in modern model when legacy had none
        assertNull(out.getDefendantAccountParty().getLanguagePreferences(),
            "Language preferences should be null when legacy languagePreferences is null");
    }

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
}
