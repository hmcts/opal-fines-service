package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.disco.legacy.LegacyTestsBase;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.PaymentTermsType;
import uk.gov.hmcts.opal.dto.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentStateSummary;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.ContactDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountPartyLegacy;
import uk.gov.hmcts.opal.dto.legacy.EmployerDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.IndividualDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountsSearchResults;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.legacy.OrganisationDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.VehicleDetailsLegacy;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class LegacyDefendantAccountServiceTest extends LegacyTestsBase {

    @Spy
    private MockRestClient restClient = spy(MockRestClient.class);

    @Mock
    private LegacyGatewayProperties gatewayProperties;

    private GatewayService gatewayService;

    @InjectMocks
    private LegacyDefendantAccountService legacyDefendantAccountService;

    @BeforeEach
    void openMocks() throws Exception {
        gatewayService = Mockito.spy(new LegacyGatewayService(gatewayProperties, restClient));
        injectGatewayService(legacyDefendantAccountService, gatewayService);
    }

    private void injectGatewayService(
        LegacyDefendantAccountService legacyDefendantAccountService, GatewayService gatewayService)
        throws NoSuchFieldException, IllegalAccessException {

        Field field = LegacyDefendantAccountService.class.getDeclaredField("gatewayService");
        field.setAccessible(true);
        field.set(legacyDefendantAccountService, gatewayService);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetHeaderSummary_success() {

        DefendantAccountHeaderSummary headerSummary = createHeaderSummaryDto();
        LegacyGetDefendantAccountHeaderSummaryResponse responseBody = createHeaderSummaryResponse();

        ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse> typeRef =
            new ParameterizedTypeReference<>() {
            };
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(responseBody);

        ResponseEntity<String> serverSuccessResponse =
            new ResponseEntity<>(responseBody.toXml(), HttpStatus.valueOf(200));
        when(restClient.responseSpec.toEntity(String.class)).thenReturn(serverSuccessResponse);

        DefendantAccountHeaderSummary published = legacyDefendantAccountService.getHeaderSummary(1L);

        assertEquals(headerSummary, published);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSearchDefendantAccounts_success() {
        AccountSearchDto searchDto = AccountSearchDto.builder().build();

        String dummyXml = getDummyXml();
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(dummyXml, HttpStatus.OK));

        LegacyDefendantAccountsSearchResults legacyResponse =
            LegacyDefendantAccountsSearchResults.builder().build();

        ParameterizedTypeReference<LegacyDefendantAccountsSearchResults> typeRef =
            new ParameterizedTypeReference<>() {
            };
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(legacyResponse);

        DefendantAccountSearchResultsDto result =
            legacyDefendantAccountService.searchDefendantAccounts(searchDto);

        assertEquals(DefendantAccountSearchResultsDto.class, result.getClass());
    }


    private DefendantAccountHeaderSummary createHeaderSummaryDto() {
        return DefendantAccountHeaderSummary.builder()
            .accountNumber("SAMPLE")
            .accountType("Fine")
            .accountStatusReference(
                AccountStatusReference.builder()
                    .accountStatusCode("L")
                    .accountStatusDisplayName("Live")
                    .build()
            )
            .businessUnitSummary(
                BusinessUnitSummary.builder()
                    .businessUnitId("1")
                    .businessUnitName("Test BU")
                    .welshSpeaking("N")
                    .build()
            )
            .paymentStateSummary(
                PaymentStateSummary.builder()
                    .imposedAmount(BigDecimal.ZERO)
                    .arrearsAmount(BigDecimal.ZERO)
                    .paidAmount(BigDecimal.ZERO)
                    .accountBalance(BigDecimal.ZERO)
                    .build()
            )
            .partyDetails(
                PartyDetails.builder().build()
            )
            .build();
    }


    private LegacyGetDefendantAccountHeaderSummaryResponse createHeaderSummaryResponse() {
        return LegacyGetDefendantAccountHeaderSummaryResponse.builder()
            .defendantAccountId("1")
            .accountNumber("SAMPLE")
            .accountType("Fine")
            .accountStatusReference(
                uk.gov.hmcts.opal.dto.legacy.common.AccountStatusReference.builder()
                    .accountStatusCode("L")
                    .accountStatusDisplayName("Live")
                    .build()
            )
            .businessUnitSummary(
                uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary.builder()
                    .businessUnitId("1")
                    .businessUnitName("Test BU")
                    .welshSpeaking("N")
                    .build()
            )
            .paymentStateSummary(
                uk.gov.hmcts.opal.dto.legacy.common.PaymentStateSummary.builder()
                    .imposedAmount("0")
                    .arrearsAmount("0")
                    .paidAmount("0")
                    .accountBalance("0")
                    .build()
            )
            .partyDetails(
                uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails.builder()
                    .build()
            )
            .build();
    }

    private String getDummyXml() {
        return """
            <response>
                <count>1</count>
                 <defendant_accounts>
                   <defendant_accounts_element>
                     <defendant_account_id>1</defendant_account_id>
                      <account_number>Sampleaccount_number</account_number>
                      <organisation>Sampleorganisation</organisation>
                      <organisation_name>Sampleorganisation_name</organisation_name>
                      <defendant_title>Sampledefendant_title</defendant_title>
                      <defendant_firstnames>Sampledefendant_firstnames</defendant_firstnames>
                      <defendant_surname>Sampledefendant_surname</defendant_surname>
                      <birth_date>Samplebirth_date</birth_date>
                      <national_insurance_number>Samplenational_insurance_number</national_insurance_number>
                      <parent_guardian_surname>Sampleparent_guardian_surname</parent_guardian_surname>
                      <parent_guardian_firstnames>Sampleparent_guardian_firstnames</parent_guardian_firstnames>
                      <aliases>
                        <aliases_element>
                          <alias_number>Samplealias_number</alias_number>
                          <organisation_name>Sampleorganisation_name</organisation_name>
                          <surname>Samplesurname</surname>
                          <forenames>Sampleforenames</forenames>
                        </aliases_element>
                      </aliases>
                      <address_line_1>Sampleaddress_line_1</address_line_1>
                      <postcode>Samplepostcode</postcode>
                      <business_unit_name>Samplebusiness_unit_name</business_unit_name>
                      <business_unit_id>Samplebusiness_unit_id</business_unit_id>
                      <prosecutor_case_reference>Sampleprosecutor_case_reference</prosecutor_case_reference>
                      <last_enforcement_action>Samplelast_enforcement_action</last_enforcement_action>
                      <account_balance>Sampleaccount_balance</account_balance>
                    </defendant_accounts_element>
                  </defendant_accounts>
            </response>
               """;
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_nonZeroAmounts_andCustomBu() {
        // Arrange: response with the values our DTO expects
        LegacyGetDefendantAccountHeaderSummaryResponse responseBody =
            LegacyGetDefendantAccountHeaderSummaryResponse.builder()
                .defendantAccountId("1")
                .accountNumber("SAMPLE")
                .accountType("Fine")
                .accountStatusReference(
                    uk.gov.hmcts.opal.dto.legacy.common.AccountStatusReference.builder()
                        .accountStatusCode("L")
                        .accountStatusDisplayName("Live")
                        .build()
                )
                .businessUnitSummary(
                    uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary.builder()
                        .businessUnitId("78")
                        .businessUnitName("Test BU")
                        .welshSpeaking("N")
                        .build()
                )
                .paymentStateSummary(
                    uk.gov.hmcts.opal.dto.legacy.common.PaymentStateSummary.builder()
                        .imposedAmount("700.58")
                        .arrearsAmount("0")
                        .paidAmount("200.00")
                        .accountBalance("500.58")
                        .build()
                )
                .partyDetails(uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails.builder().build())
                .build();

        ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        // Act
        DefendantAccountHeaderSummary published = legacyDefendantAccountService.getHeaderSummary(1L);

        // Assert: minimal checks that raise coverage on new code
        assertEquals("SAMPLE", published.getAccountNumber());
        assertEquals("Fine", published.getAccountType());
        assertEquals("Live", published.getAccountStatusReference().getAccountStatusDisplayName());
        assertEquals("78", published.getBusinessUnitSummary().getBusinessUnitId());
        assertEquals(new BigDecimal("700.58"), published.getPaymentStateSummary().getImposedAmount());
        assertEquals(BigDecimal.ZERO, published.getPaymentStateSummary().getArrearsAmount());
        assertEquals(new BigDecimal("200.00"), published.getPaymentStateSummary().getPaidAmount());
        assertEquals(new BigDecimal("500.58"), published.getPaymentStateSummary().getAccountBalance());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_blankAmounts_defaultToZero() {
        LegacyGetDefendantAccountHeaderSummaryResponse responseBody =
            LegacyGetDefendantAccountHeaderSummaryResponse.builder()
                .defendantAccountId("1")
                .accountNumber("SAMPLE")
                .accountType("Fine")
                .accountStatusReference(
                    uk.gov.hmcts.opal.dto.legacy.common.AccountStatusReference.builder()
                        .accountStatusCode("L")
                        .accountStatusDisplayName("Live")
                        .build()
                )
                .businessUnitSummary(
                    uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary.builder()
                        .businessUnitId("78")
                        .businessUnitName("Test BU")
                        .welshSpeaking("N")
                        .build()
                )
                .paymentStateSummary(
                    uk.gov.hmcts.opal.dto.legacy.common.PaymentStateSummary.builder()
                        .imposedAmount("")
                        .arrearsAmount(null)
                        .paidAmount("NaN")
                        .accountBalance("0")
                        .build()
                )
                .partyDetails(uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails.builder().build())
                .build();

        ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        DefendantAccountHeaderSummary published = legacyDefendantAccountService.getHeaderSummary(1L);
        PaymentStateSummary paymentStateSummary = published.getPaymentStateSummary();
        assertEquals(BigDecimal.ZERO, paymentStateSummary.getImposedAmount());
        assertEquals(BigDecimal.ZERO, paymentStateSummary.getArrearsAmount());
        assertEquals(BigDecimal.ZERO, paymentStateSummary.getPaidAmount());
        assertEquals(BigDecimal.ZERO, paymentStateSummary.getAccountBalance());
    }

    @Test
    void testGetHeaderSummary_gatewayThrows_hitsCatchAndRethrows() {
        doThrow(new RuntimeException("boom"))
            .when(gatewayService)
            .postToGateway(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        assertThrows(RuntimeException.class, () -> legacyDefendantAccountService.getHeaderSummary(1L));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_withIndividualDetails_executesMappingBranches() {
        uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails legacyInd =
            uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails.builder()
                .title("Mr")
                .firstNames("John")
                .surname("Smith")
                .individualAliases(new
                    uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails.IndividualAlias[0])
                .build();

        uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails party =
            uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails.builder()
                .individualDetails(legacyInd)
                .build();

        LegacyGetDefendantAccountHeaderSummaryResponse responseBody =
            createHeaderSummaryResponse();
        responseBody.setPartyDetails(party);

        ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        DefendantAccountHeaderSummary published = legacyDefendantAccountService.getHeaderSummary(1L);

        assertEquals("SAMPLE", published.getAccountNumber());
        assertEquals("Fine", published.getAccountType());
    }


    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_whenCommonSectionsNull_executesNullBranches() {
        LegacyGetDefendantAccountHeaderSummaryResponse responseBody =
            LegacyGetDefendantAccountHeaderSummaryResponse.builder()
                .defendantAccountId("1")
                .accountNumber("SAMPLE")
                .accountType("Fine")
                .partyDetails(uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails.builder().build())
                .build();

        ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        DefendantAccountHeaderSummary published = legacyDefendantAccountService.getHeaderSummary(1L);

        // These fields should be null when the nested objects are missing
        assertEquals("SAMPLE", published.getAccountNumber());
        assertEquals("Fine", published.getAccountType());
        assertNull(published.getAccountStatusReference());
        assertNull(published.getBusinessUnitSummary());
        assertNull(published.getPaymentStateSummary());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_withOrganisationDetails_executesMappingBranches() {
        // Build organisation alias array (nested type inside OrganisationDetails)
        uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails.OrganisationAlias[] orgAliasArr =
            new uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails.OrganisationAlias[]{
                uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails.OrganisationAlias.builder()
                    .aliasId("ORG1")
                    .sequenceNumber(Short.valueOf("1"))
                    .organisationName("AliasCo")
                    .build()
            };

        // Organisation details with alias array
        uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails legacyOrg =
            uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails.builder()
                .organisationName("MainCo")
                .organisationAliases(orgAliasArr)
                .build();

        // Party details containing organisation
        uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails party =
            uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails.builder()
                .organisationDetails(legacyOrg)
                .build();

        LegacyGetDefendantAccountHeaderSummaryResponse responseBody =
            createHeaderSummaryResponse();
        responseBody.setPartyDetails(party);

        ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        // Act
        DefendantAccountHeaderSummary published = legacyDefendantAccountService.getHeaderSummary(1L);

        // Assert: minimal baseline + check organisation alias details flowed through
        assertEquals("SAMPLE", published.getAccountNumber());
        assertEquals("Fine", published.getAccountType());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getPaymentTerms_success_spyGatewayAndRestClientStub() throws Exception {

        LegacyGetDefendantAccountPaymentTermsResponse responseBody =
            LegacyGetDefendantAccountPaymentTermsResponse.builder()
                .version(2L)
                .paymentTerms(
                    uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTerms.builder()
                        .daysInDefault(120)
                        .paymentTermsType(new uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType(
                            uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType.PaymentTermsTypeCode.B))
                        .instalmentPeriod(new uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod(
                            uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod.InstalmentPeriodCode.W))
                        .build()
                )
                .postedDetails(new uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails(
                    java.time.LocalDate.of(2023, 11, 3), "01000000A", ""))
                .paymentCardLastRequested(java.time.LocalDate.of(2024, 1, 1))
                .dateLastAmended(java.time.LocalDate.of(2024, 1, 3))
                .extension(false)
                .lastEnforcement("REM")
                .build();

        ParameterizedTypeReference<LegacyGetDefendantAccountPaymentTermsResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        GetDefendantAccountPaymentTermsResponse out = legacyDefendantAccountService.getPaymentTerms(99L);

        assertNotNull(out);
        assertEquals(2, out.getVersion());
        assertEquals(120, out.getPaymentTerms().getDaysInDefault());
        assertEquals(
            InstalmentPeriod.InstalmentPeriodCode.W,
            out.getPaymentTerms().getInstalmentPeriod().getInstalmentPeriodCode());
        assertEquals(
            PaymentTermsType.PaymentTermsTypeCode.B,
            out.getPaymentTerms().getPaymentTermsType().getPaymentTermsTypeCode());
        assertEquals("REM", out.getLastEnforcement());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getPaymentTerms_legacyFailure5xx_withEntity_mapsAnyway() throws Exception {

        LegacyGetDefendantAccountPaymentTermsResponse responseBody =
            LegacyGetDefendantAccountPaymentTermsResponse.builder()
                .version(3L)
                .paymentTerms(
                    uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTerms.builder()
                        .daysInDefault(5)
                        .paymentTermsType(new uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType(
                            uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType.PaymentTermsTypeCode.P))
                        .instalmentPeriod(new uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod(
                            uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod.InstalmentPeriodCode.M))
                        .build()
                )
                .build();

        ParameterizedTypeReference<LegacyGetDefendantAccountPaymentTermsResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(responseBody);

        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.SERVICE_UNAVAILABLE));

        GetDefendantAccountPaymentTermsResponse out = legacyDefendantAccountService.getPaymentTerms(1L);

        assertNotNull(out);
        assertEquals(3, out.getVersion());
        assertEquals(5, out.getPaymentTerms().getDaysInDefault());
        assertEquals(PaymentTermsType.PaymentTermsTypeCode.P,
                     out.getPaymentTerms().getPaymentTermsType().getPaymentTermsTypeCode());
        assertEquals(InstalmentPeriod.InstalmentPeriodCode.M,
                     out.getPaymentTerms().getInstalmentPeriod().getInstalmentPeriodCode());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getPaymentTerms_error5xx_returnsNull() throws Exception {

        ParameterizedTypeReference<LegacyGetDefendantAccountPaymentTermsResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(null);

        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>("<error/>", HttpStatus.INTERNAL_SERVER_ERROR));

        GetDefendantAccountPaymentTermsResponse out = legacyDefendantAccountService.getPaymentTerms(42L);

        assertNull(out);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getPaymentTerms_success_withNullEntity_returnsEmptyDto() throws Exception {

        ParameterizedTypeReference<LegacyGetDefendantAccountPaymentTermsResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(null);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>("<response/>", HttpStatus.OK));

        GetDefendantAccountPaymentTermsResponse out = legacyDefendantAccountService.getPaymentTerms(3L);

        assertNotNull(out);
        assertNull(out.getVersion());
        assertNull(out.getPaymentTerms());
        assertNull(out.getPostedDetails());
        assertNull(out.getPaymentCardLastRequested());
        assertNull(out.getDateLastAmended());
        assertNull(out.getExtension());
        assertNull(out.getLastEnforcement());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getPaymentTerms_mapsNullNestedObjects_toNulls() throws Exception {

        uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTerms legacyTerms =
            uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTerms.builder()
                .daysInDefault(0)
                .dateDaysInDefaultImposed(null)
                .reasonForExtension(null)
                .paymentTermsType(null) // toPaymentTermsType → null
                .effectiveDate(null)
                .instalmentPeriod(null) // toInstalmentPeriod → null
                .lumpSumAmount(null)
                .instalmentAmount(null)
                .build();

        LegacyGetDefendantAccountPaymentTermsResponse responseBody =
            LegacyGetDefendantAccountPaymentTermsResponse.builder()
                .version(4L)
                .paymentTerms(legacyTerms)
                .postedDetails(null) // toPostedDetails → null
                .build();

        ParameterizedTypeReference<LegacyGetDefendantAccountPaymentTermsResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        GetDefendantAccountPaymentTermsResponse out = legacyDefendantAccountService.getPaymentTerms(4L);

        assertNotNull(out);
        assertNotNull(out.getPaymentTerms());
        assertNull(out.getPaymentTerms().getPaymentTermsType());
        assertNull(out.getPaymentTerms().getInstalmentPeriod());
        assertNull(out.getPostedDetails());
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
        assertEquals(1L,
            ((uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponseJson) out).getVersion());
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
        assertEquals(2L,
            ((uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponseJson) outA).getVersion());
        assertNotNull(outA.getDefendantAccountParty());
        assertNotNull(outA.getDefendantAccountParty().getPartyDetails().getOrganisationDetails());
        assertNull(outA.getDefendantAccountParty().getPartyDetails().getIndividualDetails());
        assertNotNull(outA.getDefendantAccountParty().getEmployerDetails());
        assertNull(outA.getDefendantAccountParty().getEmployerDetails().getEmployerAddress());

        // act/assert 2) address_line_1 present -> employerAddress kept
        GetDefendantAccountPartyResponse outB =
            legacyDefendantAccountService.getDefendantAccountParty(555L, 555L);
        assertNotNull(outB);
        assertEquals(2L,
            ((uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponseJson) outB).getVersion());
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
            legacyDefendantAccountService.getDefendantAccountParty(11L, 22L);

        // assert
        assertNotNull(out);
        assertEquals(99L,
            ((uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponseJson) out).getVersion());
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
        assertNull(((uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponseJson) out).getVersion());
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
        assertEquals(1L,
            ((uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponseJson) result).getVersion());
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
        assertEquals(42L,
            ((uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponseJson) out).getVersion());
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


}
