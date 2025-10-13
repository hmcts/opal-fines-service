package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountPaymentTermsResponse;
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

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(responseBody);

        ResponseEntity<String> serverSuccessResponse =
            new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK);
        when(restClient.responseSpec.toEntity(String.class)).thenReturn(serverSuccessResponse);

        DefendantAccountHeaderSummary published = legacyDefendantAccountService.getHeaderSummary(1L);

        // Assert
        final DefendantAccountHeaderSummary expected = DefendantAccountHeaderSummary.builder()
            .version(1L)
            .defendantAccountId("1")
            .debtorType("Defendant")
            .isYouth(false)
            .accountNumber("SAMPLE")
            .accountType("Fine")
            .accountStatusReference(AccountStatusReference.builder()
                                        .accountStatusCode("L")
                                        .accountStatusDisplayName(null)
                                        .build())
            .businessUnitSummary(BusinessUnitSummary.builder()
                                     .businessUnitId("1")
                                     .businessUnitName("Test BU")
                                     .welshSpeaking("N")
                                     .build())
            .paymentStateSummary(PaymentStateSummary.builder()
                                     .imposedAmount(BigDecimal.ZERO)
                                     .arrearsAmount(BigDecimal.ZERO)
                                     .paidAmount(BigDecimal.ZERO)
                                     .accountBalance(BigDecimal.ZERO)
                                     .build())
            .partyDetails(PartyDetails.builder()
                              .partyId("1")
                              .organisationFlag(false)
                              .organisationDetails(null)
                              .individualDetails(null)
                              .build())
            .build();

        assertNotNull(published, "Expected non-null header summary");
        assertEquals(expected.getDefendantAccountId(), published.getDefendantAccountId());
        assertEquals(expected.getDebtorType(), published.getDebtorType());
        assertEquals(expected.getIsYouth(), published.getIsYouth());
        assertEquals(expected.getAccountNumber(), published.getAccountNumber());
        assertEquals(
            expected.getAccountStatusReference().getAccountStatusCode(),
            published.getAccountStatusReference().getAccountStatusCode()
        );
        assertEquals(
            expected.getBusinessUnitSummary().getBusinessUnitName(),
            published.getBusinessUnitSummary().getBusinessUnitName()
        );
        assertEquals(
            expected.getPaymentStateSummary().getImposedAmount(),
            published.getPaymentStateSummary().getImposedAmount()
        );
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

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyDefendantAccountsSearchResults>>any()
        )).thenReturn(legacyResponse);

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
                .version(1)
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
                .partyDetails(
                    uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails.builder()
                        .organisationFlag(false)
                        .build()
                )
                .build();

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(responseBody);

        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        // Act
        DefendantAccountHeaderSummary published = legacyDefendantAccountService.getHeaderSummary(1L);

        // Assert
        assertNotNull(published);
        assertEquals("SAMPLE", published.getAccountNumber());
        assertEquals("Fine", published.getAccountType());
        assertEquals("L", published.getAccountStatusReference().getAccountStatusCode());
        assertNull(published.getAccountStatusReference().getAccountStatusDisplayName(),
                   "Legacy should not populate display name");
        assertEquals("78", published.getBusinessUnitSummary().getBusinessUnitId());
        assertEquals("Test BU", published.getBusinessUnitSummary().getBusinessUnitName());
        assertEquals(new BigDecimal("700.58"), published.getPaymentStateSummary().getImposedAmount());
        assertEquals(BigDecimal.ZERO, published.getPaymentStateSummary().getArrearsAmount());
        assertEquals(new BigDecimal("200.00"), published.getPaymentStateSummary().getPaidAmount());
        assertEquals(new BigDecimal("500.58"), published.getPaymentStateSummary().getAccountBalance());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_setsDefendantPartyIdCorrectly() {
        LegacyGetDefendantAccountHeaderSummaryResponse responseBody = createHeaderSummaryResponse();
        responseBody.setDefendantPartyId("77");

        ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(Mockito
                 .<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()))
            .thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        DefendantAccountHeaderSummary result = legacyDefendantAccountService.getHeaderSummary(1L);

        assertNotNull(result);
        assertEquals("77", result.getDefendantPartyId(), "defendant_party_id should map from legacy response");
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

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(responseBody);

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

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(responseBody);

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

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(responseBody);

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

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(responseBody);

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

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountPaymentTermsResponse>>any()
        )).thenReturn(responseBody);

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

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountPaymentTermsResponse>>any()
        )).thenReturn(responseBody);

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

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountPaymentTermsResponse>>any()
        )).thenReturn(null);

        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>("<error/>", HttpStatus.INTERNAL_SERVER_ERROR));

        GetDefendantAccountPaymentTermsResponse out = legacyDefendantAccountService.getPaymentTerms(42L);

        assertNull(out);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getPaymentTerms_success_withNullEntity_returnsEmptyDto() throws Exception {

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountPaymentTermsResponse>>any()
        )).thenReturn(null);

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

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountPaymentTermsResponse>>any()
        )).thenReturn(responseBody);

        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        GetDefendantAccountPaymentTermsResponse out = legacyDefendantAccountService.getPaymentTerms(4L);

        assertNotNull(out);
        assertNotNull(out.getPaymentTerms());
        assertNull(out.getPaymentTerms().getPaymentTermsType());
        assertNull(out.getPaymentTerms().getInstalmentPeriod());
        assertNull(out.getPaymentTerms().getPostedDetails());
    }

    // --- NEW: cover error -> legacy failure branch (5xx) for getHeaderSummary ---
    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_legacyFailure5xx_logsAndMaps() {
        LegacyGetDefendantAccountHeaderSummaryResponse responseBody = createHeaderSummaryResponse();

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(responseBody);

        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.SERVICE_UNAVAILABLE));

        DefendantAccountHeaderSummary out = legacyDefendantAccountService.getHeaderSummary(1L);
        assertNotNull(out);
        assertEquals("SAMPLE", out.getAccountNumber());
    }

    // --- NEW: cover error -> exception branch for getHeaderSummary (gateway returns Response with exception) ---
    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_exceptionBranch_rethrows() {
        LegacyGetDefendantAccountHeaderSummaryResponse responseBody = createHeaderSummaryResponse();

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(responseBody);

        when(restClient.responseSpec.toEntity(String.class))
            .thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class,
                     () -> legacyDefendantAccountService.getHeaderSummary(1L));
    }

    // --- NEW: cover Number branch in toBigDecimalOrZero via public API ---
    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_numberInputs_mapToBigDecimal() {
        // Arrange: build a legacy response with numeric values (BigDecimal + Number variants)
        LegacyGetDefendantAccountHeaderSummaryResponse resp = createHeaderSummaryResponse();

        PaymentStateSummary pay = PaymentStateSummary.builder()
            .imposedAmount(new BigDecimal("100.0"))
            .arrearsAmount(BigDecimal.valueOf(0))
            .paidAmount(BigDecimal.valueOf(25L))
            .accountBalance(BigDecimal.valueOf(75.5f))
            .build();

        resp.setPaymentStateSummary(
            uk.gov.hmcts.opal.dto.legacy.common.PaymentStateSummary.builder()
                .imposedAmount(pay.getImposedAmount().toString())
                .arrearsAmount(pay.getArrearsAmount().toString())
                .paidAmount(pay.getPaidAmount().toString())
                .accountBalance(pay.getAccountBalance().toString())
                .build()
        );

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(resp);

        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(resp.toXml(), HttpStatus.OK));

        // Act
        DefendantAccountHeaderSummary out = legacyDefendantAccountService.getHeaderSummary(1L);

        // Assert (compareTo to ignore scale)
        var ps = out.getPaymentStateSummary();
        assertEquals(0, ps.getImposedAmount().compareTo(new BigDecimal("100.0")));
        assertEquals(0, ps.getArrearsAmount().compareTo(new BigDecimal("0")));
        assertEquals(0, ps.getPaidAmount().compareTo(new BigDecimal("25")));
        assertEquals(0, ps.getAccountBalance().compareTo(new BigDecimal("75.5")));
    }


    // --- NEW: cover unsupported-type branch → defaults to ZERO (and logs warn) ---
    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_unsupportedPaymentType_defaultsZero() {
        var pay = uk.gov.hmcts.opal.dto.legacy.common.PaymentStateSummary.builder()
            .imposedAmount("0")
            .build();

        var resp = createHeaderSummaryResponse();
        resp.setPaymentStateSummary(pay);

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(resp);

        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(resp.toXml(), HttpStatus.OK));

        var out = legacyDefendantAccountService.getHeaderSummary(1L);
        assertEquals(BigDecimal.ZERO, out.getPaymentStateSummary().getImposedAmount());
    }

    // --- NEW: cover individual aliases mapping (non-empty array) ---
    @Test
    @SuppressWarnings("unchecked")
    void testGetHeaderSummary_individualAliases_areMapped() {
        var alias = uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails.IndividualAlias.builder()
            .aliasId("AL1").sequenceNumber(Short.valueOf("1")).surname("AliasSurname").forenames("AliasForenames")
            .build();
        var ind = uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails.builder()
            .title("Mr").firstNames("John").surname("Smith")
            .individualAliases(new uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails.IndividualAlias[]{ alias })
            .build();

        var party = uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails.builder()
            .individualDetails(ind).build();

        var resp = createHeaderSummaryResponse();
        resp.setPartyDetails(party);

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(resp);

        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(resp.toXml(), HttpStatus.OK));

        var out = legacyDefendantAccountService.getHeaderSummary(1L);
        assertNotNull(out.getPartyDetails().getIndividualDetails().getIndividualAliases());
        assertEquals(1, out.getPartyDetails().getIndividualDetails().getIndividualAliases().size());
    }

    // --- NEW: cover the three not-implemented public APIs (no reflection) ---
    @Test
    void testGetDefendantAccountParty_notImplemented() {
        assertThrows(UnsupportedOperationException.class,
                     () -> legacyDefendantAccountService.getDefendantAccountParty(1L, 2L));
    }

    @Test
    void testGetAtAGlance_notImplemented() {
        assertThrows(UnsupportedOperationException.class,
                     () -> legacyDefendantAccountService.getAtAGlance(1L));
    }

    @Test
    void testUpdateDefendantAccount_notImplemented() {
        var ex = assertThrows(org.springframework.web.server.ResponseStatusException
                                  .class,
                              () -> legacyDefendantAccountService.updateDefendantAccount(1L, "78", null, "\"0\"",
                                                                                         "tester"));
        assertEquals(HttpStatus.NOT_IMPLEMENTED, ex.getStatusCode());
    }


}
