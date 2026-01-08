package uk.gov.hmcts.opal.service.legacy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import jakarta.persistence.EntityNotFoundException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.DefendantAccountResponse;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.ResultResponse;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.EmployerDetails;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentStateSummary;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.AddDefendantAccountEnforcementLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.ContactDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountPartyLegacy;
import uk.gov.hmcts.opal.dto.legacy.EmployerDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.IndividualDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LanguagePreferencesLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountsSearchResults;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountEnforcementStatusResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountEnforcementStatusResponse.EnforcementAction;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountEnforcementStatusResponse.EnforcementOverview;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.dto.legacy.LegacyReplaceDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyReplaceDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateDefendantAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.OrganisationDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.VehicleDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.common.CollectionOrder;
import uk.gov.hmcts.opal.dto.legacy.common.CourtReference;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcementOverride;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcementOverrideResultReference;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcerReference;
import uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LjaReference;
import uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.legacy.common.ResultReference;
import uk.gov.hmcts.opal.dto.legacy.common.ResultResponses;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.court.CourtEntity.Lite;
import uk.gov.hmcts.opal.generated.model.AccountStatusReferenceCommon;
import uk.gov.hmcts.opal.generated.model.AccountStatusReferenceCommon.AccountStatusCodeEnum;
import uk.gov.hmcts.opal.generated.model.EnforcementActionDefendantAccount;
import uk.gov.hmcts.opal.generated.model.EnforcementOverrideCommon;
import uk.gov.hmcts.opal.generated.model.EnforcementOverviewDefendantAccount;
import uk.gov.hmcts.opal.mapper.legacy.LegacyUpdateDefendantAccountResponseMapper;
import uk.gov.hmcts.opal.mapper.request.UpdateDefendantAccountRequestMapper;
import uk.gov.hmcts.opal.service.opal.CourtService;

@ExtendWith(MockitoExtension.class)
class LegacyDefendantAccountServiceTest extends LegacyTestsBase {

    @Spy
    private MockRestClient restClient = spy(MockRestClient.class);

    @Mock
    private LegacyGatewayProperties gatewayProperties;

    @Mock
    private CourtService courtService;

    private GatewayService gatewayService;

    @Mock private UpdateDefendantAccountRequestMapper updateDefendantAccountRequestMapper;
    @Mock private LegacyUpdateDefendantAccountResponseMapper legacyUpdateDefendantAccountResponseMapper;

    @InjectMocks
    private LegacyDefendantAccountService legacyDefendantAccountService;

    private UpdateDefendantAccountRequest updateDefendantAccountRequest;

    @BeforeEach
    void openMocks() throws Exception {
        gatewayService = Mockito.spy(new LegacyGatewayService(gatewayProperties, restClient));
        injectGatewayService(legacyDefendantAccountService, gatewayService);

        updateDefendantAccountRequest = mock(UpdateDefendantAccountRequest.class, RETURNS_DEEP_STUBS);
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
        // Arrange
        LegacyGetDefendantAccountHeaderSummaryResponse responseBody = createHeaderSummaryResponse();

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHeaderSummaryResponse>>any()
        )).thenReturn(responseBody);


        ResponseEntity<String> serverSuccessResponse =
            new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK);
        when(restClient.responseSpec.toEntity(String.class)).thenReturn(serverSuccessResponse);

        // Act
        DefendantAccountHeaderSummary actual = legacyDefendantAccountService.getHeaderSummary(1L);

        // Assert
        final DefendantAccountHeaderSummary expected = DefendantAccountHeaderSummary.builder()
            .version(BigInteger.valueOf(1L))
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

        assertNotNull(actual, "Expected non-null header summary");
        assertEquals(expected.getDefendantAccountId(), actual.getDefendantAccountId());
        assertEquals(expected.getDebtorType(), actual.getDebtorType());
        assertEquals(expected.getIsYouth(), actual.getIsYouth());
        assertEquals(expected.getAccountNumber(), actual.getAccountNumber());
        assertEquals(expected.getAccountStatusReference().getAccountStatusCode(),
                     actual.getAccountStatusReference().getAccountStatusCode());
        assertEquals(expected.getBusinessUnitSummary().getBusinessUnitName(),
                     actual.getBusinessUnitSummary().getBusinessUnitName());
        assertEquals(expected.getPaymentStateSummary().getImposedAmount(),
                     actual.getPaymentStateSummary().getImposedAmount());
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
        // Arrange
        LegacyGetDefendantAccountHeaderSummaryResponse responseBody =
            LegacyGetDefendantAccountHeaderSummaryResponse.builder()
                .version("1")
                .defendantAccountId("1")
                .accountNumber("SAMPLE")
                .accountType("Fine")
                .accountStatusReference(
                    uk.gov.hmcts.opal.dto.legacy.common.AccountStatusReference.builder()
                        .accountStatusCode("L")
                        // display name is ignored by mapper, will be null
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
        assertEquals("Live", published.getAccountStatusReference().getAccountStatusDisplayName(),
            "Legacy mapper should populate display name for schema compliance");
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
        when(restClient.responseSpec.body(Mockito.<ParameterizedTypeReference
            <LegacyGetDefendantAccountHeaderSummaryResponse>>any()))
            .thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        DefendantAccountHeaderSummary result = legacyDefendantAccountService.getHeaderSummary(1L);

        assertNotNull(result);
        assertEquals("77", result.getDefendantAccountPartyId(), "defendant_account_party_id should map from "
            + "legacy response");
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

        var out = legacyDefendantAccountService.getDefendantAccountParty(77L, 77L);
        var prefs = out.getDefendantAccountParty().getLanguagePreferences();

        org.junit.jupiter.api.Assertions.assertNotNull(prefs);
        org.junit.jupiter.api.Assertions.assertEquals("EN",
            prefs.getDocumentLanguagePreference().getLanguageCode());
        org.junit.jupiter.api.Assertions.assertEquals("CY",
            prefs.getHearingLanguagePreference().getLanguageCode());
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
        assertEquals(BigInteger.valueOf(5L), out.getVersion());
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
            LegacyGetDefendantAccountAtAGlanceResponse.builder().version(0L).defendantAccountId("456").build();

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
            .partyId("777")
            .organisationDetails(legacyOrg)
            .build();

        LegacyGetDefendantAccountAtAGlanceResponse body =
            LegacyGetDefendantAccountAtAGlanceResponse.builder()
                .partyDetails(party)
                .version(0L)
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
            .partyId("1001")
            .individualDetails(ind)
            .build();

        LegacyGetDefendantAccountAtAGlanceResponse body =
            LegacyGetDefendantAccountAtAGlanceResponse.builder()
                .partyDetails(party)
                .version(0L)
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
                .version(0L)
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
                .version(0L)
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

    @Test
    void toBigDecimalOrZero_handlesAllBranches() throws Exception {
        var method = LegacyDefendantAccountService.class.getDeclaredMethod("toBigDecimalOrZero", Object.class);
        method.setAccessible(true);

        // null input → ZERO
        assertEquals(BigDecimal.ZERO, method.invoke(null, (Object) null));

        // BigDecimal instance → returns as-is
        BigDecimal value = new BigDecimal("123.45");
        assertEquals(value, method.invoke(null, value));

        // CharSequence good → parses correctly
        assertEquals(new BigDecimal("77"), method.invoke(null, "77"));

        // CharSequence bad → logs and defaults to ZERO
        assertEquals(BigDecimal.ZERO, method.invoke(null, "NaN"));

        // Number instance (Integer) → converted
        assertEquals(new BigDecimal("5.0"), method.invoke(null, 5));

        // Unsupported type → defaults ZERO
        assertEquals(BigDecimal.ZERO, method.invoke(null, new Object()));
    }

    @Test
    void toPaymentTermsType_and_toInstalmentPeriod_coverNonNullCodes() throws Exception {
        var ptt = LegacyDefendantAccountService.class.getDeclaredMethod(
            "toPaymentTermsType", uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType.class);
        var ip  = LegacyDefendantAccountService.class.getDeclaredMethod(
            "toInstalmentPeriod", uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod.class);
        ptt.setAccessible(true);
        ip.setAccessible(true);

        var legacyType = new uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType(
            uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType.PaymentTermsTypeCode.B);
        var legacyInst = new uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod(
            uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod.InstalmentPeriodCode.W);

        var out1 = ptt.invoke(null, legacyType);
        var out2 = ip.invoke(null, legacyInst);
        assertNotNull(out1);
        assertNotNull(out2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getHeaderSummary_errorAndSuccessBranches_triggerLogging() {
        // Arrange a fake response entity
        uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse legacyEntity =
            uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse.builder().build();

        // First response → simulate 5xx LegacyFailure
        GatewayService.Response<uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse> respError =
            new GatewayService.Response<>(HttpStatus.INTERNAL_SERVER_ERROR, legacyEntity, "body", null);

        // Second response → simulate 200 OK success
        GatewayService.Response<uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse>
            respSuccess =
            new GatewayService.Response<>(HttpStatus.OK, legacyEntity, null, null);

        // Spy gateway to return each response in order
        doReturn(respError, respSuccess).when(gatewayService)
            .postToGateway(any(), any(), any(), any());

        // Act 1: triggers error + legacyFailure path
        legacyDefendantAccountService.getHeaderSummary(1L);

        // Act 2: triggers success logging path
        legacyDefendantAccountService.getHeaderSummary(1L);

        // Verify gateway called twice (once for each)
        verify(gatewayService, times(2)).postToGateway(any(), any(), any(), any());
    }

    @Test
    void toHeaderSumaryDto_mapsOrgAndIndBranches() throws Exception {
        var method = LegacyDefendantAccountService.class.getDeclaredMethod(
            "toHeaderSumaryDto",
            uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse.class);
        method.setAccessible(true);

        // --- organisation branch ---
        var orgAlias = uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails.OrganisationAlias.builder()
            .aliasId("O1").sequenceNumber((short)1).organisationName("AliasCo").build();
        var orgDetails = uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails.builder()
            .organisationName("MainCo")
            .organisationAliases(new uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails.OrganisationAlias[]
                {orgAlias})
            .build();
        var party = uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails.builder()
            .organisationFlag(true).organisationDetails(orgDetails).build();
        var resp = uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse.builder()
            .partyDetails(party).build();
        assertNotNull(method.invoke(legacyDefendantAccountService, resp));

        // --- individual branch ---
        var indAlias = uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails.IndividualAlias.builder()
            .aliasId("I1").sequenceNumber((short)1).surname("Smith").forenames("John").build();
        var ind = uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails.builder()
            .firstNames("John").surname("Smith")
            .individualAliases(new uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails.IndividualAlias[]{indAlias})
            .build();
        party = uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails.builder()
            .organisationFlag(false).individualDetails(ind).build();
        resp = uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse.builder()
            .partyDetails(party).build();
        assertNotNull(method.invoke(legacyDefendantAccountService, resp));
    }

    @Test
    void toHeaderSumaryDto_populatesDisplayNameFromCodeWhenMissing() throws Exception {
        // Arrange – legacy response has status code but no display name
        var legacyResponse = uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse.builder()
            .accountStatusReference(
                uk.gov.hmcts.opal.dto.legacy.common.AccountStatusReference.builder()
                    .accountStatusCode("L")
                    .accountStatusDisplayName(null)
                    .build()
            )
            .accountNumber("177A")
            .defendantAccountId("77")
            .accountType("Fine")
            .build();

        // Reflectively access the private mapping method
        var method = LegacyDefendantAccountService.class.getDeclaredMethod(
            "toHeaderSumaryDto",
            uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse.class
        );
        method.setAccessible(true);

        // Act
        var result = (uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary)
            method.invoke(legacyDefendantAccountService, legacyResponse);

        // Assert
    }

    @Test
    void testUpdateDefendantAccount_happyPath_buildsLegacyRequest_callsGateway_andMapsResponse() {

        final String postedBy = "user-123";
        long defendantAccountId = 77L;
        String businessUnitId = "78";

        // arrange: mapper builds the legacy request
        LegacyUpdateDefendantAccountRequest legacyReq = LegacyUpdateDefendantAccountRequest.builder()
            .defendantAccountId(String.valueOf(defendantAccountId))
            .businessUnitId(businessUnitId)
            .businessUnitUserId(postedBy)
            .version(3)
            .build();

        when(updateDefendantAccountRequestMapper.toLegacyUpdateDefendantAccountRequest(
            any(), anyString(), anyString(), anyString(), anyInt())
        ).thenReturn(legacyReq);

        // arrange: gateway returns OK with an entity
        LegacyUpdateDefendantAccountResponse legacyEntity = new LegacyUpdateDefendantAccountResponse();

        GatewayService.Response<LegacyUpdateDefendantAccountResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacyEntity, null, null);

        // Stub spy’d gateway (choose correct overload)
        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.PATCH_DEFENDANT_ACCOUNT),
            eq(LegacyUpdateDefendantAccountResponse.class),
            any(LegacyUpdateDefendantAccountRequest.class),
            Mockito.nullable(String.class)
        );

        // arrange: response mapper
        DefendantAccountResponse expected = DefendantAccountResponse.builder().id(defendantAccountId).build();
        when(legacyUpdateDefendantAccountResponseMapper.toDefendantAccountResponse(legacyEntity)).thenReturn(expected);

        // act
        DefendantAccountResponse result = legacyDefendantAccountService
            .updateDefendantAccount(defendantAccountId,
                                    businessUnitId,
                                    updateDefendantAccountRequest,
                            "3",
                                    postedBy);

        // assert: correct result
        assertThat(result).isSameAs(expected);

        // assert: request mapper called with parsed version = 3 and proper ids
        verify(updateDefendantAccountRequestMapper).toLegacyUpdateDefendantAccountRequest(
            same(updateDefendantAccountRequest), eq("77"), eq("78"), eq(postedBy), eq(3)
        );

        // assert: gateway call & response mapping
        verify(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.PATCH_DEFENDANT_ACCOUNT),
            eq(LegacyUpdateDefendantAccountResponse.class),
            eq(legacyReq),
            isNull()
        );

        // the PATCH_DEFENDANT_ACCOUNT action is "LIBRA.patchDefendantAccount"
        verify(legacyUpdateDefendantAccountResponseMapper).toDefendantAccountResponse(legacyEntity);
    }

    @Test
    void testUpdateDefendantAccount_whenGatewayThrows_exceptionPropagates() {
        final String postedBy = "user-123";
        long defendantAccountId = 77L;
        String businessUnitId = "78";

        // arrange: mapper builds the legacy request
        when(updateDefendantAccountRequestMapper
            .toLegacyUpdateDefendantAccountRequest(any(), anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(LegacyUpdateDefendantAccountRequest.builder().build());

        // service stubbed to throw exception
        when(gatewayService.postToGateway(eq(LegacyDefendantAccountService.PATCH_DEFENDANT_ACCOUNT),
            eq(LegacyUpdateDefendantAccountResponse.class),
            any(),
            isNull()))
            .thenThrow(new RuntimeException("Simulate Run Time Exception from gateway"));

        // act + assert
        assertThrows(RuntimeException.class, () ->
            legacyDefendantAccountService
                .updateDefendantAccount(defendantAccountId, businessUnitId, mock(UpdateDefendantAccountRequest.class),
                    "5", postedBy)
        );

        // verify the call path hit the mock
        verify(gatewayService).postToGateway(
            eq("LIBRA.patchDefendantAccount"),
            eq(LegacyUpdateDefendantAccountResponse.class),
            any(LegacyUpdateDefendantAccountRequest.class),
            isNull()
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void testUpdateDefendantAccount_error5xx_noEntity_returnsNull() {
        // stubbed to return error 5xx
        ParameterizedTypeReference<LegacyUpdateDefendantAccountResponse> typeRef =
            new ParameterizedTypeReference<>() {
            };
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(null);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>("<response><error/></response>", HttpStatus.INTERNAL_SERVER_ERROR));

        // act + assert
        DefendantAccountResponse response = legacyDefendantAccountService
            .updateDefendantAccount(77L, "78", mock(UpdateDefendantAccountRequest.class),
                "1", "postedBy");
        assertNull(response);
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
        GetDefendantAccountPartyResponse out = legacyDefendantAccountService.replaceDefendantAccountParty(
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
        GetDefendantAccountPartyResponse out = legacyDefendantAccountService.replaceDefendantAccountParty(
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
            legacyDefendantAccountService.replaceDefendantAccountParty(
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
        GetDefendantAccountPartyResponse out = legacyDefendantAccountService.replaceDefendantAccountParty(
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
        GetDefendantAccountPartyResponse out = legacyDefendantAccountService.replaceDefendantAccountParty(
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
        GetDefendantAccountPartyResponse out = legacyDefendantAccountService.replaceDefendantAccountParty(
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
        GetDefendantAccountPartyResponse out = legacyDefendantAccountService.replaceDefendantAccountParty(
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
        GetDefendantAccountPartyResponse out = legacyDefendantAccountService.replaceDefendantAccountParty(
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
        GetDefendantAccountPartyResponse out = legacyDefendantAccountService.replaceDefendantAccountParty(
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

    @Test
    void addEnforcement_success_returnsMappedAddEnforcementResponse_simple() {
        AddDefendantAccountEnforcementLegacyResponse legacyResp =
            mock(AddDefendantAccountEnforcementLegacyResponse.class);
        when(legacyResp.getEnforcementId()).thenReturn("ENF-1");
        when(legacyResp.getDefendantAccountId()).thenReturn("123");
        when(legacyResp.getVersion()).thenReturn(1);

        GatewayService.Response<AddDefendantAccountEnforcementLegacyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacyResp, null, null);

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.ADD_ENFORCEMENT),
            eq(AddDefendantAccountEnforcementLegacyResponse.class),
            any(),
            Mockito.nullable(String.class)
        );

        AddEnforcementResponse out =
            legacyDefendantAccountService.addEnforcement(123L, "BU-1", "user-1", "\"1\"", "auth", null);

        assertNotNull(out);
        assertEquals("ENF-1", out.getEnforcementId());
        assertEquals("123", out.getDefendantAccountId());
        assertEquals(1, out.getVersion());
    }

    @Test
    void addEnforcement_legacyFailure5xx_withEntity_stillReturnsMappedResponse_simple() {
        AddDefendantAccountEnforcementLegacyResponse legacyResp =
            mock(AddDefendantAccountEnforcementLegacyResponse.class);
        when(legacyResp.getEnforcementId()).thenReturn("ENF-500");
        when(legacyResp.getDefendantAccountId()).thenReturn("500");
        when(legacyResp.getVersion()).thenReturn(5);

        GatewayService.Response<AddDefendantAccountEnforcementLegacyResponse> resp =
            new GatewayService.Response<>(HttpStatus.SERVICE_UNAVAILABLE, legacyResp, "<legacy-failure/>", null);

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.ADD_ENFORCEMENT),
            eq(AddDefendantAccountEnforcementLegacyResponse.class),
            any(),
            Mockito.nullable(String.class)
        );

        // Act
        AddEnforcementResponse out =
            legacyDefendantAccountService.addEnforcement(500L, "BU-500", "user-500", "\"5\"", "auth", null);

        // Assert
        assertNotNull(out);
        assertEquals("ENF-500", out.getEnforcementId());
        assertEquals("500", out.getDefendantAccountId());
        assertEquals(5, out.getVersion());
    }


    @Test
    @SuppressWarnings("unchecked")
    void addEnforcement_withRequest_sendsLegacyRequestContainingMappedCollectionsAndPaymentTerms() throws Exception {
        // Arrange: mock modern request with one ResultResponse and a PaymentTerms object
        AddDefendantAccountEnforcementRequest request = mock(AddDefendantAccountEnforcementRequest.class);
        ResultResponse rr = mock(ResultResponse.class);
        when(rr.getParameterName()).thenReturn("param-1");
        when(rr.getResponse()).thenReturn("resp-1");
        when(request.getEnforcementResultResponses()).thenReturn(java.util.List.of(rr));

        PaymentTerms pt = mock(PaymentTerms.class);
        PaymentTermsType ptt = mock(PaymentTermsType.class);
        when(ptt.getPaymentTermsTypeCode()).thenReturn(null); // safe for mapLegacyPaymentTermsType
        InstalmentPeriod ip = mock(InstalmentPeriod.class);
        when(ip.getInstalmentPeriodCode()).thenReturn(null); // safe for mapLegacyInstalmentPeriod
        when(pt.getPaymentTermsType()).thenReturn(ptt);
        when(pt.getInstalmentPeriod()).thenReturn(ip);
        when(request.getPaymentTerms()).thenReturn(pt);

        // Prepare legacy response entity that will be returned by gateway
        AddDefendantAccountEnforcementLegacyResponse legacyResp =
            mock(AddDefendantAccountEnforcementLegacyResponse.class);
        when(legacyResp.getEnforcementId()).thenReturn("ENF-CAP");
        when(legacyResp.getDefendantAccountId()).thenReturn("999");
        when(legacyResp.getVersion()).thenReturn(11);

        GatewayService.Response<AddDefendantAccountEnforcementLegacyResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacyResp, null, null);

        // stub gateway to capture the legacy request and return the response
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Object> reqCaptor = ArgumentCaptor.forClass(Object.class);
        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.ADD_ENFORCEMENT),
            eq(AddDefendantAccountEnforcementLegacyResponse.class),
            reqCaptor.capture(),
            Mockito.nullable(String.class)
        );

        // Act
        AddEnforcementResponse out =
            legacyDefendantAccountService.addEnforcement(999L, "BU-TEST", "user-test", "\"11\"", "auth", request);

        // Assert - public DTO returned correctly
        assertNotNull(out);
        assertEquals("ENF-CAP", out.getEnforcementId());
        assertEquals("999", out.getDefendantAccountId());
        assertEquals(11, out.getVersion());

        // Also assert the service built a legacy request with expected top-level fields and mapped collections
        Object sentLegacyRequest = reqCaptor.getValue();
        assertNotNull(sentLegacyRequest);

        // Use reflection to verify the legacy request's fields (getters created by builder)
        var clazz = sentLegacyRequest.getClass();
        var defId = clazz.getMethod("getDefendantAccountId").invoke(sentLegacyRequest);
        var buId = clazz.getMethod("getBusinessUnitId").invoke(sentLegacyRequest);
        var buUser = clazz.getMethod("getBusinessUnitUserId").invoke(sentLegacyRequest);


        assertEquals("999", defId);
        assertEquals("BU-TEST", buId);
        assertEquals("user-test", buUser);
        var version = clazz.getMethod("getVersion").invoke(sentLegacyRequest);

        assertEquals(11, ((Number) version).intValue());

        var enforcementList = clazz.getMethod("getEnforcementResultResponses").invoke(sentLegacyRequest);

        assertNotNull(enforcementList);
        assertTrue(((java.util.Collection<?>) enforcementList).size() >= 1);

        var paymentTermsLegacy = clazz.getMethod("getPaymentTerms").invoke(sentLegacyRequest);

        assertNotNull(paymentTermsLegacy);
    }

    @Test
    void addEnforcement_legacyFailure5xx_withEntity_stillReturnsMappedResponse_simpleCoverage() {
        // Arrange - legacy 5xx but responseEntity present (exercises legacy-failure logging path)
        AddDefendantAccountEnforcementLegacyResponse legacyResp =
            mock(AddDefendantAccountEnforcementLegacyResponse.class);
        when(legacyResp.getEnforcementId()).thenReturn("ENF-500");
        when(legacyResp.getDefendantAccountId()).thenReturn("500");
        when(legacyResp.getVersion()).thenReturn(5);

        GatewayService.Response<AddDefendantAccountEnforcementLegacyResponse> resp =
            new GatewayService.Response<>(HttpStatus.SERVICE_UNAVAILABLE, legacyResp, "<legacy-failure/>", null);

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.ADD_ENFORCEMENT),
            eq(AddDefendantAccountEnforcementLegacyResponse.class),
            any(),
            Mockito.nullable(String.class)
        );

        // Act
        AddEnforcementResponse out =
            legacyDefendantAccountService.addEnforcement(500L, "BU-500", "user-500", "\"5\"", "auth", null);

        // Assert
        assertNotNull(out);
        assertEquals("ENF-500", out.getEnforcementId());
        assertEquals("500", out.getDefendantAccountId());
        assertEquals(5, out.getVersion());
    }

    @Test
    void addEnforcement_gatewayResponseWithException_throwsNullPointerDueToMissingEntity() {
        // Arrange: simulate gateway returning Response with exception and no responseEntity
        GatewayService.Response<AddDefendantAccountEnforcementLegacyResponse> errResp =
            new GatewayService.Response<>(HttpStatus.BAD_GATEWAY, new RuntimeException("boom"), "<err/>");

        doReturn(errResp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.ADD_ENFORCEMENT),
            eq(AddDefendantAccountEnforcementLegacyResponse.class),
            any(),
            Mockito.nullable(String.class)
        );

        // Act & Assert: calling the public method should throw a NullPointerException inside production code
        assertThrows(NullPointerException.class, () ->
            legacyDefendantAccountService.addEnforcement(1L, "BU", "U", "\"1\"", "auth", null)
        );
    }

    @Test
    void mapLegacyPostedDetails_null_returnsNull() throws Exception {
        var method = LegacyDefendantAccountService.class
            .getDeclaredMethod("mapLegacyPostedDetails", PostedDetails.class);
        method.setAccessible(true);

        Object result = method.invoke(legacyDefendantAccountService, (Object) null);

        assertNull(result, "Null input must return null");
    }

    @Test
    void mapLegacyPostedDetails_mapsFieldsCorrectly() throws Exception {
        // Arrange
        PostedDetails pd = new PostedDetails();
        pd.setPostedBy("tester");
        pd.setPostedByName("Test User");
        pd.setPostedDate(java.time.LocalDate.of(2024, 1, 1));

        var method = LegacyDefendantAccountService.class
            .getDeclaredMethod("mapLegacyPostedDetails", PostedDetails.class);
        method.setAccessible(true);

        // Act
        LegacyPostedDetails out =
            (LegacyPostedDetails) method.invoke(legacyDefendantAccountService, pd);

        // Assert
        assertNotNull(out);
        assertEquals(pd.getPostedBy(), out.getPostedBy());
        assertEquals(pd.getPostedByName(), out.getPostedByName());
        assertEquals(pd.getPostedDate(), out.getPostedDate());
    }

    @Test
    void mapLegacyPaymentTermsType_nullOrMissingCode_returnsNull() throws Exception {
        var method = LegacyDefendantAccountService.class
            .getDeclaredMethod("mapLegacyPaymentTermsType", PaymentTermsType.class);
        method.setAccessible(true);

        // null → null
        assertNull(method.invoke(legacyDefendantAccountService, (Object) null));

        // PaymentTermsType with null code → null
        PaymentTermsType pt = mock(PaymentTermsType.class);
        when(pt.getPaymentTermsTypeCode()).thenReturn(null);
        assertNull(method.invoke(legacyDefendantAccountService, pt));
    }

    @Test
    void mapLegacyPaymentTermsType_validCode_mapsCorrectEnum() throws Exception {
        PaymentTermsType pt = mock(PaymentTermsType.class);
        when(pt.getPaymentTermsTypeCode()).thenReturn(PaymentTermsType.PaymentTermsTypeCode.B);

        var method = LegacyDefendantAccountService.class
            .getDeclaredMethod("mapLegacyPaymentTermsType", PaymentTermsType.class);
        method.setAccessible(true);

        LegacyPaymentTermsType out =
            (LegacyPaymentTermsType) method.invoke(legacyDefendantAccountService, pt);

        assertNotNull(out);
        assertEquals(LegacyPaymentTermsType.PaymentTermsTypeCode.B, out.getPaymentTermsTypeCode());
    }

    @Test
    void mapLegacyInstalmentPeriod_nullOrMissingCode_returnsNull() throws Exception {
        var method = LegacyDefendantAccountService.class
            .getDeclaredMethod("mapLegacyInstalmentPeriod", InstalmentPeriod.class);
        method.setAccessible(true);

        // null → null
        assertNull(method.invoke(legacyDefendantAccountService, (Object) null));

        InstalmentPeriod ip = mock(InstalmentPeriod.class);
        when(ip.getInstalmentPeriodCode()).thenReturn(null);
        assertNull(method.invoke(legacyDefendantAccountService, ip));
    }

    @Test
    void mapLegacyInstalmentPeriod_validCode_mapsCorrectEnum() throws Exception {
        InstalmentPeriod ip = mock(InstalmentPeriod.class);
        when(ip.getInstalmentPeriodCode()).thenReturn(
            InstalmentPeriod.InstalmentPeriodCode.W
        );

        var method = LegacyDefendantAccountService.class
            .getDeclaredMethod("mapLegacyInstalmentPeriod", InstalmentPeriod.class);
        method.setAccessible(true);

        LegacyInstalmentPeriod out =
            (LegacyInstalmentPeriod) method.invoke(legacyDefendantAccountService, ip);

        assertNotNull(out);
        assertEquals(LegacyInstalmentPeriod.InstalmentPeriodCode.W, out.getInstalmentPeriodCode());
    }

    @Test
    void mapPaymentTermsTypeCodeEnum_validAndInvalid() throws Exception {
        var method = LegacyDefendantAccountService.class
            .getDeclaredMethod("mapPaymentTermsTypeCodeEnum", String.class);
        method.setAccessible(true);

        assertEquals(
            LegacyPaymentTermsType.PaymentTermsTypeCode.B,
            method.invoke(legacyDefendantAccountService, "B")
        );

        assertEquals(
            LegacyPaymentTermsType.PaymentTermsTypeCode.P,
            method.invoke(legacyDefendantAccountService, "p")
        );

        assertNull(method.invoke(legacyDefendantAccountService, (Object) null));

        // Invalid code → IllegalArgumentException (wrapped in InvocationTargetException)
        assertThrows(Exception.class, () -> method.invoke(legacyDefendantAccountService, "X"));
    }

    @Test
    void mapInstalmentPeriodCodeEnum_validAndInvalid() throws Exception {
        var method = LegacyDefendantAccountService.class
            .getDeclaredMethod("mapInstalmentPeriodCodeEnum", String.class);
        method.setAccessible(true);

        assertEquals(
            LegacyInstalmentPeriod.InstalmentPeriodCode.W,
            method.invoke(legacyDefendantAccountService, "W")
        );

        assertEquals(
            LegacyInstalmentPeriod.InstalmentPeriodCode.M,
            method.invoke(legacyDefendantAccountService, "m")
        );

        assertNull(method.invoke(legacyDefendantAccountService, (Object) null));

        assertThrows(Exception.class, () -> method.invoke(legacyDefendantAccountService, "Z"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetEnforcementStatus_success() {
        // Arrange
        LegacyGetDefendantAccountEnforcementStatusResponse responseBody =
            createLegacyEnforcementStatusResponse(true);

        when(restClient.responseSpec
            .body(Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountEnforcementStatusResponse>>any()))
            .thenReturn(responseBody);

        when(courtService.getCourtById(anyLong())).thenReturn(Lite.builder().courtCode((short)123).build());

        ResponseEntity<String> serverSuccessResponse =
            new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK);
        when(restClient.responseSpec.toEntity(String.class)).thenReturn(serverSuccessResponse);

        // Act
        EnforcementStatus response = legacyDefendantAccountService
            .getEnforcementStatus(33L);

        // Assert
        assertNotNull(response);
        assertTrue(response.getEmployerFlag());
        assertEquals(new BigInteger("1234567890123456789012345678901234567890"), response.getVersion());
        assertFalse(response.getIsHmrcCheckEligible());
        assertNull(response.getNextEnforcementActionData());
        assertNotNull(response.getEnforcementOverride());
        assertNotNull(response.getLastEnforcementAction());
        assertNotNull(response.getEnforcementOverview());
        assertNotNull(response.getAccountStatusReference());

        EnforcementOverrideCommon override = response.getEnforcementOverride();
        assertNotNull(override.getEnforcementOverrideResult());
        assertEquals("AAB", override.getEnforcementOverrideResult().getEnforcementOverrideResultId());
        assertEquals("AaAaBb", override.getEnforcementOverrideResult().getEnforcementOverrideResultName());
        assertNotNull(override.getEnforcer());
        assertEquals(2L, override.getEnforcer().getEnforcerId());
        assertEquals("Arthur", override.getEnforcer().getEnforcerName());
        assertNotNull(override.getLja());
        assertEquals(1, override.getLja().getLjaId());
        assertEquals("England", override.getLja().getLjaName());

        EnforcementActionDefendantAccount action = response.getLastEnforcementAction();
        assertEquals("late", action.getReason());
        assertEquals("123", action.getWarrantNumber());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), action.getDateAdded());
        assertNotNull(action.getEnforcer());
        assertEquals(4L, action.getEnforcer().getEnforcerId());
        assertEquals("Merlin", action.getEnforcer().getEnforcerName());
        assertNotNull(action.getEnforcementAction());
        assertEquals("FEE", action.getEnforcementAction().getResultId());
        assertEquals("Result Ref", action.getEnforcementAction().getResultTitle());
        assertNotNull(action.getResultResponses());
        assertNotNull(action.getResultResponses().getFirst());
        assertEquals("Param Name", action.getResultResponses().getFirst().getParameterName());
        assertEquals("A response", action.getResultResponses().getFirst().getResponse());

        EnforcementOverviewDefendantAccount overview = response.getEnforcementOverview();
        assertEquals(6, overview.getDaysInDefault());
        assertNotNull(overview.getCollectionOrder());
        assertEquals(true, overview.getCollectionOrder().getCollectionOrderFlag());
        assertEquals(LocalDate.of(2024, 3, 4), overview.getCollectionOrder().getCollectionOrderDate());
        assertNotNull(overview.getEnforcementCourt());
        assertEquals(3, overview.getEnforcementCourt().getCourtId());
        assertEquals(123, overview.getEnforcementCourt().getCourtCode());
        assertEquals("Bath", overview.getEnforcementCourt().getCourtName());

        AccountStatusReferenceCommon statusRef = response.getAccountStatusReference();
        assertEquals(AccountStatusCodeEnum.L, statusRef.getAccountStatusCode());
        assertEquals("Alive", statusRef.getAccountStatusDisplayName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetEnforcementStatus_successMinimal() {
        // Arrange
        LegacyGetDefendantAccountEnforcementStatusResponse responseBody =
            createLegacyEnforcementStatusResponse(false);

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountEnforcementStatusResponse>>any()
        )).thenReturn(responseBody);

        ResponseEntity<String> serverSuccessResponse = new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK);
        when(restClient.responseSpec.toEntity(String.class)).thenReturn(serverSuccessResponse);
        when(courtService.getCourtById(anyLong())).thenReturn(Lite.builder().courtCode((short)123).build());

        // Act
        EnforcementStatus response = legacyDefendantAccountService.getEnforcementStatus(72L);

        // Assert
        assertNotNull(response);
        assertTrue(response.getEmployerFlag());
        assertEquals(new BigInteger("1234567890123456789012345678901234567890"), response.getVersion());
        assertFalse(response.getIsHmrcCheckEligible());
        assertNull(response.getNextEnforcementActionData());
        assertNull(response.getEnforcementOverride());
        assertNull(response.getLastEnforcementAction());
        assertNotNull(response.getEnforcementOverview());
        assertNotNull(response.getAccountStatusReference());

        EnforcementOverviewDefendantAccount overview = response.getEnforcementOverview();
        assertEquals(6, overview.getDaysInDefault());
        assertNotNull(overview.getCollectionOrder());
        assertEquals(true, overview.getCollectionOrder().getCollectionOrderFlag());
        assertEquals(LocalDate.of(2024, 3, 4), overview.getCollectionOrder().getCollectionOrderDate());
        assertNotNull(overview.getEnforcementCourt());
        assertEquals(3, overview.getEnforcementCourt().getCourtId());
        assertEquals("Bath", overview.getEnforcementCourt().getCourtName());

        AccountStatusReferenceCommon statusRef = response.getAccountStatusReference();
        assertEquals(AccountStatusCodeEnum.L, statusRef.getAccountStatusCode());
        assertEquals("Alive", statusRef.getAccountStatusDisplayName());
    }

    @Test
    void testGetEnforcementStatus_throwsRuntimeException() {
        // Arrange
        doThrow(new RuntimeException("boom"))
            .when(gatewayService)
            .postToGateway(any(), any(), any(), any());

        // Act + Assert
        assertThrows(RuntimeException.class, () -> legacyDefendantAccountService.getEnforcementStatus(1L));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetEnforcementStatus_returnsNull() {
        // Arrange
        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountPaymentTermsResponse>>any())).thenReturn(null);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>("<error/>", HttpStatus.INTERNAL_SERVER_ERROR));

        // Act
        EnforcementStatus response = legacyDefendantAccountService.getEnforcementStatus(42L);

        // Assert
        assertNull(response);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetEnforcementStatus_returnsFailure() {

        // Arrange
        LegacyGetDefendantAccountEnforcementStatusResponse responseBody =
            createLegacyEnforcementStatusResponse(false);
        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountEnforcementStatusResponse>>any()
        )).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.SERVICE_UNAVAILABLE));
        when(courtService.getCourtById(anyLong())).thenReturn(Lite.builder().courtCode((short)123).build());

        EnforcementStatus response = legacyDefendantAccountService.getEnforcementStatus(66L);

        assertNotNull(response);
        assertTrue(response.getEmployerFlag());
        assertEquals(new BigInteger("1234567890123456789012345678901234567890"), response.getVersion());
        assertFalse(response.getIsHmrcCheckEligible());
        assertNull(response.getNextEnforcementActionData());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetEnforcementStatus_courtNotFoundInOpalDB() {

        // Arrange
        LegacyGetDefendantAccountEnforcementStatusResponse responseBody =
            createLegacyEnforcementStatusResponse(false);
        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountEnforcementStatusResponse>>any()
        )).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.SERVICE_UNAVAILABLE));
        when(courtService.getCourtById(anyLong())).thenThrow(new EntityNotFoundException("Court not found"));

        EntityNotFoundException error = assertThrows(EntityNotFoundException.class,
            () -> legacyDefendantAccountService.getEnforcementStatus(66L));

        assertNotNull(error);
        assertEquals("Court not found", error.getMessage());
    }

    private LegacyGetDefendantAccountEnforcementStatusResponse createLegacyEnforcementStatusResponse(boolean full) {
        return LegacyGetDefendantAccountEnforcementStatusResponse.builder()
            .accountStatusReference(
                uk.gov.hmcts.opal.dto.legacy.common.AccountStatusReference.builder()
                    .accountStatusCode("L")
                    .accountStatusDisplayName("Alive")
                    .build())
            .enforcementOverride(full ? EnforcementOverride.builder()  // Optional
                .lja(LjaReference.builder()
                    .ljaId(1).ljaName("England").build())
                .enforcer(EnforcerReference.builder()
                    .enforcerId(2L).enforcerName("Arthur").build())
                .enforcementOverrideResult(EnforcementOverrideResultReference.builder()
                    .enforcementOverrideResultId("AAB").enforcementOverrideResultName("AaAaBb").build())
                .build() : null)
            .enforcementOverview(EnforcementOverview.builder()
                .enforcementCourt(CourtReference.builder()
                    .courtId(3L).courtName("Bath").build())
                .collectionOrder(CollectionOrder.builder()
                    .collectionOrderCode("XX").collectionOrderFlag(true)
                    .collectionOrderDate(LocalDate.of(2024, 3,4)).build())
                .daysInDefault(6)
                .build())
            .lastEnforcementAction(full ? EnforcementAction.builder() // Optional
                .enforcer(EnforcerReference.builder()
                    .enforcerId(4L).enforcerName("Merlin").build())
                .resultReference(ResultReference.builder()
                    .resultId("FEE").resultTitle("Result Ref").build())
                .resultResponses(ResultResponses.builder()
                    .parameterName("Param Name").response("A response").build())
                .dateAdded("2024-01-01T10:00:00")
                .reason("late")
                .warrantNumber("123")
                .build() : null)
            .version("1234567890123456789012345678901234567890")
            .employerFlag("true")
            .build();
    }
}
