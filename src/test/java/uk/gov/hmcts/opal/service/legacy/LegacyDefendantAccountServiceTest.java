package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.Assertions;
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
import uk.gov.hmcts.opal.disco.legacy.LegacyGatewayService;
import uk.gov.hmcts.opal.disco.legacy.LegacyTestsBase;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.legacy.LegacyCreateDefendantAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountsSearchResults;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.legacy.common.DefendantDetails;
import uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.legacy.common.PaymentStateSummary;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    void testPublishDefendantAccount_success() {

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
    void testSearchDefendantAccounts_success() {

        AccountSearchDto searchDto = AccountSearchDto.builder().build();

        String dummyXml = "<LegacyDefendantAccountsSearchResults><search_results/><total_count>0</total_count></LegacyDefendantAccountsSearchResults>";
        ResponseEntity<String> serverSuccessResponse =
            new ResponseEntity<>(dummyXml, HttpStatus.OK);
        when(restClient.responseSpec.toEntity(String.class)).thenReturn(serverSuccessResponse);

        LegacyDefendantAccountsSearchResults legacyResponse =
            LegacyDefendantAccountsSearchResults.builder().build();

        ParameterizedTypeReference<LegacyDefendantAccountsSearchResults> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(legacyResponse);

        DefendantAccountSearchResultsDto result =
            legacyDefendantAccountService.searchDefendantAccounts(searchDto);

        assertEquals(DefendantAccountSearchResultsDto.class, result.getClass());
    }

    @Test
    void shouldThrowWhenLegacyGatewayFails() {
        AccountSearchDto searchDto = AccountSearchDto.builder().build();

        GatewayService.Response<LegacyDefendantAccountsSearchResults> failedResponse =
            new GatewayService.Response<>(HttpStatus.INTERNAL_SERVER_ERROR, (LegacyDefendantAccountsSearchResults) null);

        GatewayService spyGatewayService = Mockito.spy(gatewayService);
        when(spyGatewayService.postToGateway(
            any(String.class),
            eq(LegacyDefendantAccountsSearchResults.class),
            any()
        )).thenReturn(failedResponse);

        try {
            injectGatewayService(legacyDefendantAccountService, spyGatewayService);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Injection failed", e);
        }

        Assertions.assertThrows(RuntimeException.class, () -> {
            legacyDefendantAccountService.searchDefendantAccounts(searchDto);
        });
    }

    private DefendantAccountHeaderSummary createHeaderSummaryDto() {
        return DefendantAccountHeaderSummary.builder()
            .hasParentGuardian(false)
            .imposed(BigDecimal.ZERO)
            .arrears(BigDecimal.ZERO)
            .paid(BigDecimal.ZERO)
            .writtenOff(BigDecimal.ZERO)
            .accountBalance(BigDecimal.ZERO)
            .build();
    }

    private LegacyGetDefendantAccountHeaderSummaryResponse createHeaderSummaryResponse() {
        return LegacyGetDefendantAccountHeaderSummaryResponse.builder()
            .defendantDetails(
                DefendantDetails.builder()
                    .organisationDetails(OrganisationDetails.builder().build())
                    .individualDetails(IndividualDetails.builder().build())
                    .build())
            .accountStatusReference(AccountStatusReference.builder().build())
            .businessUnitSummary(BusinessUnitSummary.builder().build())
            .paymentStateSummary(PaymentStateSummary.builder().build())
            .build();
    }
}
