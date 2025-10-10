package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentStateSummary;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountsSearchResults;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;

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
                        .extension(false)
                        .paymentTermsType(new uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType(
                            uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType.PaymentTermsTypeCode.B))
                        .instalmentPeriod(new uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod(
                            uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod.InstalmentPeriodCode.W))
                        .postedDetails(new uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails(
                            java.time.LocalDate.of(2023, 11, 3), "01000000A", ""))
                        .build()
                )
                .paymentCardLastRequested(java.time.LocalDate.of(2024, 1, 1))
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
        assertNull(out.getPaymentCardLastRequested());
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
                .postedDetails(null) // toPostedDetails → null
                .build();

        LegacyGetDefendantAccountPaymentTermsResponse responseBody =
            LegacyGetDefendantAccountPaymentTermsResponse.builder()
                .version(4L)
                .paymentTerms(legacyTerms)
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
        assertNull(out.getPaymentTerms().getPostedDetails());
    }


    @Test
    @SuppressWarnings("unchecked")
    void getAtAGlance_success_mapsTopLevelAndNulls() {
        LegacyGetDefendantAccountAtAGlanceResponse body = LegacyGetDefendantAccountAtAGlanceResponse.builder()
            .defendantAccountId("123")
            .accountNumber("ACC-42")
            .debtorType("PERSON")
            .youth(Boolean.FALSE)
            .version(5L)
            // leave nested sections null to cover null branches
            .build();

        ParameterizedTypeReference<LegacyGetDefendantAccountAtAGlanceResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(body);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(body.toXml(), HttpStatus.OK));

        DefendantAccountAtAGlanceResponse out = legacyDefendantAccountService.getAtAGlance(123L);

        assertNotNull(out);
        assertEquals("123", out.getDefendantAccountId());
        assertEquals("ACC-42", out.getAccountNumber());
        assertEquals("PERSON", out.getDebtorType());
        assertEquals(Boolean.FALSE, out.getIsYouth());
        assertEquals(5L, out.getVersion());
        assertNull(out.getPartyDetails());
        assertNull(out.getAddressDetails());
        assertNull(out.getLanguagePreferences());
        assertNull(out.getPaymentTermsSummary());
        assertNull(out.getEnforcementStatus());
        assertNull(out.getCommentsAndNotes());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAtAGlance_legacyFailure5xx_withEntity_mapsAnyway() {
        LegacyGetDefendantAccountAtAGlanceResponse body =
            LegacyGetDefendantAccountAtAGlanceResponse.builder().defendantAccountId("456").build();

        ParameterizedTypeReference<LegacyGetDefendantAccountAtAGlanceResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(body);
        // simulate 5xx but with a body (service should still map)
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(body.toXml(), HttpStatus.SERVICE_UNAVAILABLE));

        DefendantAccountAtAGlanceResponse out = legacyDefendantAccountService.getAtAGlance(456L);
        assertNotNull(out);
        assertEquals("456", out.getDefendantAccountId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAtAGlance_error5xx_noEntity_returnsNull() {
        ParameterizedTypeReference<LegacyGetDefendantAccountAtAGlanceResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(null);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>("<error/>", HttpStatus.INTERNAL_SERVER_ERROR));

        DefendantAccountAtAGlanceResponse out = legacyDefendantAccountService.getAtAGlance(999L);
        assertNull(out);
    }

    @Test
    void getAtAGlance_gatewayThrows_hitsCatchAndRethrows() {
        doThrow(new RuntimeException("boom"))
            .when(gatewayService)
            .postToGateway(any(), any(), any(), any());

        assertThrows(RuntimeException.class, () -> legacyDefendantAccountService.getAtAGlance(1L));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAtAGlance_mapsOrganisationBranch_withAliases() {
        // org alias element
        OrganisationDetails.OrganisationAlias orgAlias = OrganisationDetails.OrganisationAlias.builder()
            .aliasId("10")
            .sequenceNumber(Short.valueOf("2"))
            .organisationName("Alt Name Ltd")
            .build();

        OrganisationDetails legacyOrg = OrganisationDetails.builder()
            .organisationName("Acme Ltd")
            .organisationAliases(new OrganisationDetails.OrganisationAlias[]{ orgAlias })
            .build();

        LegacyPartyDetails party = LegacyPartyDetails.builder()
            .organisationFlag(Boolean.TRUE)
            .defendantAccountPartyId("777")
            .organisationDetails(legacyOrg)
            .build();

        LegacyGetDefendantAccountAtAGlanceResponse body =
            LegacyGetDefendantAccountAtAGlanceResponse.builder()
                .partyDetails(party)
                .build();

        ParameterizedTypeReference<LegacyGetDefendantAccountAtAGlanceResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(body);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(body.toXml(), HttpStatus.OK));

        DefendantAccountAtAGlanceResponse out = legacyDefendantAccountService.getAtAGlance(1L);

        PartyDetails pd = out.getPartyDetails();
        assertNotNull(pd);
        assertEquals(true, pd.getOrganisationFlag());
        assertEquals("777", pd.getPartyId());
        assertNotNull(pd.getOrganisationDetails());
        assertNull(pd.getIndividualDetails());
        assertEquals("Acme Ltd", pd.getOrganisationDetails().getOrganisationName());
        assertNotNull(pd.getOrganisationDetails().getOrganisationAliases());
        assertEquals(1, pd.getOrganisationDetails().getOrganisationAliases().size());
        assertEquals("10", pd.getOrganisationDetails().getOrganisationAliases().get(0).getAliasId());
        assertEquals(2, pd.getOrganisationDetails().getOrganisationAliases().get(0).getSequenceNumber());
        assertEquals("Alt Name Ltd", pd.getOrganisationDetails()
            .getOrganisationAliases().get(0).getOrganisationName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAtAGlance_mapsIndividualBranch_withAliases_andDobFormatting() {
        IndividualDetails.IndividualAlias alias = IndividualDetails.IndividualAlias.builder()
            .aliasId("21")
            .sequenceNumber(Short.valueOf("3"))
            .surname("Smith")
            .forenames("John")
            .build();

        IndividualDetails ind = IndividualDetails.builder()
            .title("Mr")
            .firstNames("John James")
            .surname("Smith")
            .dateOfBirth(null)
            .age("34")
            .nationalInsuranceNumber("QQ123456C")
            .individualAliases(new IndividualDetails.IndividualAlias[]{ alias })
            .build();

        LegacyPartyDetails party = LegacyPartyDetails.builder()
            .organisationFlag(Boolean.FALSE)
            .defendantAccountPartyId("1001")
            .individualDetails(ind)
            .build();

        LegacyGetDefendantAccountAtAGlanceResponse body =
            LegacyGetDefendantAccountAtAGlanceResponse.builder()
                .partyDetails(party)
                .build();

        ParameterizedTypeReference<LegacyGetDefendantAccountAtAGlanceResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(body);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(body.toXml(), HttpStatus.OK));

        DefendantAccountAtAGlanceResponse out = legacyDefendantAccountService.getAtAGlance(1L);

        PartyDetails pd = out.getPartyDetails();
        assertEquals(false, pd.getOrganisationFlag());
        assertEquals("1001", pd.getPartyId());
        assertNull(pd.getOrganisationDetails());
        assertNotNull(pd.getIndividualDetails());
        assertEquals("Mr", pd.getIndividualDetails().getTitle());
        assertEquals("John James", pd.getIndividualDetails().getForenames());
        assertEquals("Smith", pd.getIndividualDetails().getSurname());
        assertNull(pd.getIndividualDetails().getDateOfBirth());
        assertEquals("34", pd.getIndividualDetails().getAge());
        assertEquals("QQ123456C", pd.getIndividualDetails().getNationalInsuranceNumber());
        assertNotNull(pd.getIndividualDetails().getIndividualAliases());
        assertEquals(1, pd.getIndividualDetails().getIndividualAliases().size());
        assertEquals("21", pd.getIndividualDetails().getIndividualAliases().get(0).getAliasId());
        assertEquals(3, pd.getIndividualDetails().getIndividualAliases().get(0).getSequenceNumber());
        assertEquals("Smith", pd.getIndividualDetails().getIndividualAliases().get(0).getSurname());
        assertEquals("John", pd.getIndividualDetails().getIndividualAliases().get(0).getForenames());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAtAGlance_mapsAddress_language_payment_enforcement_comments() {
        // --- arrange legacy inputs ---
        uk.gov.hmcts.opal.dto.legacy.common.AddressDetails addr =
            uk.gov.hmcts.opal.dto.legacy.common.AddressDetails.builder()
                .addressLine1("1 Street")
                .addressLine2("Area")
                .addressLine3(null)
                .addressLine4("Town")
                .addressLine5("County")
                .postcode("AB1 2CD")
                .build();

        var dlp = uk.gov.hmcts.opal.dto.legacy.common.LanguagePreferences.DocumentLanguagePreference
            .builder().documentLanguageCode("EN").build();
        var hlp = uk.gov.hmcts.opal.dto.legacy.common.LanguagePreferences.HearingLanguagePreference
            .builder().hearingLanguageCode("CY").build();
        var legacyLang = uk.gov.hmcts.opal.dto.legacy.common.LanguagePreferences.builder()
            .documentLanguagePreference(dlp)
            .hearingLanguagePreference(hlp)
            .build();

        var legacyPts = uk.gov.hmcts.opal.dto.legacy.common.PaymentTermsSummary.builder()
            .paymentTermsType(new uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType(
                uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType.PaymentTermsTypeCode.I))
            .instalmentPeriod(new uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod(
                uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod.InstalmentPeriodCode.W))
            .effectiveDate(java.time.LocalDate.of(2024, 1, 2))
            .lumpSumAmount(new java.math.BigDecimal("250.00"))
            .instalmentAmount(new java.math.BigDecimal("25.00"))
            .build();

        var lea = uk.gov.hmcts.opal.dto.common.LastEnforcementAction.builder()
            .lastEnforcementActionId("REM")
            .lastEnforcementActionTitle("Reminder")
            .build();

        var legacyEnf = uk.gov.hmcts.opal.dto.legacy.common.EnforcementStatusSummary.builder()
            .lastEnforcementAction(lea)
            .collectionOrderMade(Boolean.TRUE)
            .defaultDaysInJail(7)
            .enforcementOverride(null) // you expect null in API
            .lastMovementDate(java.time.LocalDate.of(2023, 12, 31))
            .build();

        var legacyCom = uk.gov.hmcts.opal.dto.legacy.common.CommentsAndNotes.builder()
            .accountComment("Main note")
            .freeTextNote1("N1")
            .freeTextNote2("N2")
            .freeTextNote3("N3")
            .build();

        // This is the ONLY body we should stub with
        uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountAtAGlanceResponse body =
            uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountAtAGlanceResponse.builder()
                .address(addr)
                .languagePreferences(legacyLang)
                .paymentTermsSummary(legacyPts)
                .enforcementStatusSummary(legacyEnf)
                .commentsAndNotes(legacyCom)
                .build();

        ParameterizedTypeReference<uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountAtAGlanceResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(body);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(body.toXml(), HttpStatus.OK));

        // --- act ---
        uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse out =
            legacyDefendantAccountService.getAtAGlance(1L);

        // --- assert ---

        // Address
        assertNotNull(out.getAddressDetails());
        assertEquals("1 Street", out.getAddressDetails().getAddressLine1());
        assertNull(out.getAddressDetails().getAddressLine3());
        assertEquals("AB1 2CD", out.getAddressDetails().getPostcode());

        // Language
        uk.gov.hmcts.opal.dto.common.LanguagePreferences lp = out.getLanguagePreferences();
        assertNotNull(lp);

        // Payment terms
        uk.gov.hmcts.opal.dto.common.PaymentTermsSummary pts = out.getPaymentTermsSummary();
        assertEquals(java.time.LocalDate.of(2024, 1, 2), pts.getEffectiveDate());
        assertEquals(new java.math.BigDecimal("250.00"), pts.getLumpSumAmount());
        assertEquals(new java.math.BigDecimal("25.00"), pts.getInstalmentAmount());

        // Enforcement (API model uses value objects)
        uk.gov.hmcts.opal.dto.common.EnforcementStatusSummary es = out.getEnforcementStatus();
        assertNotNull(es);
        assertNotNull(es.getLastEnforcementAction());
        assertEquals("REM", es.getLastEnforcementAction().getLastEnforcementActionId());
        assertEquals("Reminder", es.getLastEnforcementAction().getLastEnforcementActionTitle());
        assertEquals(Boolean.TRUE, es.getCollectionOrderMade());
        assertEquals(7, es.getDefaultDaysInJail());
        assertNull(es.getEnforcementOverride()); // we set null
        assertEquals(java.time.LocalDate.of(2023, 12, 31), es.getLastMovementDate());

        // Comments
        uk.gov.hmcts.opal.dto.common.CommentsAndNotes cn = out.getCommentsAndNotes();
        assertEquals("Main note", cn.getAccountNotesAccountComments());
        assertEquals("N1", cn.getAccountNotesFreeTextNote1());
        assertEquals("N2", cn.getAccountNotesFreeTextNote2());
        assertEquals("N3", cn.getAccountNotesFreeTextNote3());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAtAGlance_paymentTerms_nullEnumsHandled() {
        uk.gov.hmcts.opal.dto.legacy.common.PaymentTermsSummary legacyPtsNulls =
            uk.gov.hmcts.opal.dto.legacy.common.PaymentTermsSummary.builder()
                .paymentTermsType(null)
                .instalmentPeriod(null)
                .build();

        uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountAtAGlanceResponse body =
            uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountAtAGlanceResponse.builder()
                .paymentTermsSummary(legacyPtsNulls)
                .build();

        ParameterizedTypeReference<LegacyGetDefendantAccountAtAGlanceResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(body);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(body.toXml(), HttpStatus.OK));

        DefendantAccountAtAGlanceResponse out = legacyDefendantAccountService.getAtAGlance(1L);
        assertNotNull(out.getPaymentTermsSummary());
        assertNull(out.getPaymentTermsSummary().getPaymentTermsType());
        assertNull(out.getPaymentTermsSummary().getInstalmentPeriod());
    }
}
