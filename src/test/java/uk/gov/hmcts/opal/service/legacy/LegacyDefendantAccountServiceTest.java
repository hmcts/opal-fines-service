package uk.gov.hmcts.opal.service.legacy;

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
import uk.gov.hmcts.opal.dto.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentStateSummary;
import uk.gov.hmcts.opal.dto.legacy.LegacyCreateDefendantAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountsSearchResults;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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

        ParameterizedTypeReference typeRef = new ParameterizedTypeReference<LegacyCreateDefendantAccountResponse>(){};
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

        ResponseEntity<String> serverSuccessResponse =
            new ResponseEntity<>(dummyXml, HttpStatus.OK);
        when(restClient.responseSpec.toEntity(String.class)).thenReturn(serverSuccessResponse);

        LegacyDefendantAccountsSearchResults legacyResponse =
            LegacyDefendantAccountsSearchResults.builder().build();

        ParameterizedTypeReference typeRef =
            new ParameterizedTypeReference<LegacyDefendantAccountsSearchResults>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(legacyResponse);

        DefendantAccountSearchResultsDto result =
            legacyDefendantAccountService.searchDefendantAccounts(searchDto);

        assertEquals(DefendantAccountSearchResultsDto.class, result.getClass());
    }

    private DefendantAccountHeaderSummary createHeaderSummaryDto() {
        return DefendantAccountHeaderSummary.builder()
            .accountNumber("SAMPLE")
            .accountType("Fine")
            .accountStatusDisplayName("Live")
            .businessUnitId("78")
            .imposed(new BigDecimal("700.58"))
            .arrears(BigDecimal.ZERO)
            .paid(new BigDecimal("200.00"))
            .accountBalance(new BigDecimal("500.58"))
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
        </response>""";
    }
}
