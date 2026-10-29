package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.service.legacy.LegacyMajorCreditorAccountService.GET_MAJOR_CREDITOR_ACCOUNT_AT_A_GLANCE;
import static uk.gov.hmcts.opal.service.legacy.LegacyMajorCreditorAccountService.GET_MAJOR_CREDITOR_ACCOUNT_HEADER_SUMMARY;
import static uk.gov.hmcts.opal.service.legacy.LegacyMajorCreditorAccountService.GET_MAJOR_CREDITOR_ACCOUNT_HISTORY;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountAtAGlanceLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountAtAGlanceLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountAtAGlanceLegacyResponse.MajorCreditorLegacy;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyMajorCreditorHistoryResponse;
import uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.response.GetMajorCreditorHistoryResponse;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.mapper.legacy.GetMajorCreditorAccountAtAGlanceResponseLegacyMapper;
import uk.gov.hmcts.opal.mapper.legacy.GetMajorCreditorAccountHeaderSummaryResponseLegacyMapper;
import uk.gov.hmcts.opal.mapper.legacy.GetMajorCreditorAccountHistoryRequestLegacyMapper;
import uk.gov.hmcts.opal.mapper.legacy.LegacyMajorCreditorHistoryMapper;
import uk.gov.hmcts.opal.util.FeatureFlags;

@ExtendWith(MockitoExtension.class)
class LegacyMajorCreditorAccountServiceTest {

    @Mock
    private GatewayService gatewayService;

    @Mock
    private GetMajorCreditorAccountAtAGlanceResponseLegacyMapper atAGlanceResponseMapper;

    @Mock
    private GetMajorCreditorAccountHeaderSummaryResponseLegacyMapper headerSummaryResponseMapper;

    @Mock
    private GetMajorCreditorAccountHistoryRequestLegacyMapper historyRequestMapper;

    @Mock
    private LegacyMajorCreditorHistoryMapper historyResponseMapper;

    @Mock
    private LegacyBusinessUnitCodeResolver legacyBusinessUnitCodeResolver;

    @Mock
    protected FeatureToggleApi featureToggleApi;

    @InjectMocks
    private LegacyMajorCreditorAccountService legacyMajorCreditorAccountService;

    @Test
    void getAtAGlance_postsLegacyRequestMapsResponseAndSetsVersion() {
        GetMajorCreditorAccountAtAGlanceLegacyResponse legacyResponse = atAGlanceLegacyResponse();
        GetMajorCreditorAccountAtAGlanceResponse mappedResponse =
            new GetMajorCreditorAccountAtAGlanceResponse();

        GetMajorCreditorAccountAtAGlanceLegacyRequest expectedRequest = atAGlanceLegacyRequest();

        when(gatewayService.postToGateway(
            GET_MAJOR_CREDITOR_ACCOUNT_AT_A_GLANCE,
            GetMajorCreditorAccountAtAGlanceLegacyResponse.class,
            expectedRequest,
            null
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse));
        when(atAGlanceResponseMapper.toOpal(legacyResponse)).thenReturn(mappedResponse);

        GetMajorCreditorAccountAtAGlanceResponse result =
            legacyMajorCreditorAccountService.getAtAGlance(123L);

        assertEquals(mappedResponse, result);
        assertEquals(BigInteger.valueOf(7), result.getVersion());

        verify(gatewayService).postToGateway(
            GET_MAJOR_CREDITOR_ACCOUNT_AT_A_GLANCE,
            GetMajorCreditorAccountAtAGlanceLegacyResponse.class,
            expectedRequest,
            null
        );
    }

    @Test
    void getHeaderSummary_postsLegacyRequestMapsResponseAndSetsVersion() {
        GetMajorCreditorAccountHeaderSummaryLegacyResponse legacyResponse = legacyResponse();
        GetMajorCreditorAccountHeaderSummaryResponse mappedResponse =
            new GetMajorCreditorAccountHeaderSummaryResponse();
        mappedResponse.setBusinessUnitDetails(new BusinessUnitSummaryCommon()
                                                 .businessUnitId((short) 46)
                                                 .businessUnitName("Test BU"));

        GetMajorCreditorAccountHeaderSummaryLegacyRequest expectedRequest = legacyRequest();

        when(gatewayService.postToGateway(
            GET_MAJOR_CREDITOR_ACCOUNT_HEADER_SUMMARY,
            GetMajorCreditorAccountHeaderSummaryLegacyResponse.class,
            expectedRequest,
            null
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse));
        when(headerSummaryResponseMapper.toOpal(legacyResponse)).thenReturn(mappedResponse);
        when(legacyBusinessUnitCodeResolver.resolve("46", null)).thenReturn("0046");

        GetMajorCreditorAccountHeaderSummaryResponse result =
            legacyMajorCreditorAccountService.getHeaderSummary(123L);

        assertEquals(mappedResponse, result);
        assertEquals(BigInteger.valueOf(7), result.getVersion());
        assertEquals("0046", result.getBusinessUnitDetails().getBusinessUnitCode());

        verify(gatewayService).postToGateway(
            GET_MAJOR_CREDITOR_ACCOUNT_HEADER_SUMMARY,
            GetMajorCreditorAccountHeaderSummaryLegacyResponse.class,
            expectedRequest,
            null
        );
    }

    @Test
    void getHeaderSummary_handlesGatewayExceptionResponse() {
        GetMajorCreditorAccountHeaderSummaryLegacyResponse legacyResponse = legacyResponse();
        GetMajorCreditorAccountHeaderSummaryLegacyRequest expectedRequest = legacyRequest();

        when(gatewayService.postToGateway(
            GET_MAJOR_CREDITOR_ACCOUNT_HEADER_SUMMARY,
            GetMajorCreditorAccountHeaderSummaryLegacyResponse.class,
            expectedRequest,
            null
        )).thenReturn(new GatewayService.Response<>(
            HttpStatus.INTERNAL_SERVER_ERROR,
            legacyResponse,
            null,
            new RuntimeException("Gateway error")
        ));

        HttpServerErrorException exception = assertThrows(
            HttpServerErrorException.class,
            () -> legacyMajorCreditorAccountService.getHeaderSummary(123L)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        verifyNoInteractions(headerSummaryResponseMapper);
    }

    @Test
    void getHeaderSummary_handlesLegacyFailureResponse() {
        GetMajorCreditorAccountHeaderSummaryLegacyResponse legacyResponse = legacyResponse();
        GetMajorCreditorAccountHeaderSummaryLegacyRequest expectedRequest = legacyRequest();

        when(gatewayService.postToGateway(
            GET_MAJOR_CREDITOR_ACCOUNT_HEADER_SUMMARY,
            GetMajorCreditorAccountHeaderSummaryLegacyResponse.class,
            expectedRequest,
            null
        )).thenReturn(new GatewayService.Response<>(
            HttpStatus.INTERNAL_SERVER_ERROR,
            legacyResponse,
            "<legacyFailure/>",
            null
        ));

        HttpServerErrorException exception = assertThrows(
            HttpServerErrorException.class,
            () -> legacyMajorCreditorAccountService.getHeaderSummary(123L)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        verifyNoInteractions(headerSummaryResponseMapper);
    }

    @Test
    void getHeaderSummary_wrapsDirectGatewayServerException() {
        GetMajorCreditorAccountHeaderSummaryLegacyRequest expectedRequest = legacyRequest();

        when(gatewayService.postToGateway(
            GET_MAJOR_CREDITOR_ACCOUNT_HEADER_SUMMARY,
            GetMajorCreditorAccountHeaderSummaryLegacyResponse.class,
            expectedRequest,
            null
        )).thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE, "Gateway unavailable"));

        HttpServerErrorException exception = assertThrows(
            HttpServerErrorException.class,
            () -> legacyMajorCreditorAccountService.getHeaderSummary(123L)
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
        assertEquals("Gateway unavailable", exception.getStatusText());
        verifyNoInteractions(headerSummaryResponseMapper);
    }

    @Test
    void getHeaderSummary_wrapsDirectGatewayClientException() {
        GetMajorCreditorAccountHeaderSummaryLegacyRequest expectedRequest = legacyRequest();

        when(gatewayService.postToGateway(
            GET_MAJOR_CREDITOR_ACCOUNT_HEADER_SUMMARY,
            GetMajorCreditorAccountHeaderSummaryLegacyResponse.class,
            expectedRequest,
            null
        )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Account not found"));

        HttpClientErrorException exception = assertThrows(
            HttpClientErrorException.class,
            () -> legacyMajorCreditorAccountService.getHeaderSummary(123L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Account not found", exception.getStatusText());
        verifyNoInteractions(headerSummaryResponseMapper);
    }

    @Test
    void getHistory_postsLegacyRequestMapsResponseAndForcesFinancialItemTypeInR1BMode() {
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 1, 31);
        GetMajorCreditorAccountHistoryLegacyRequest expectedRequest =
            GetMajorCreditorAccountHistoryLegacyRequest.builder()
                .creditorAccountId("123")
                .fromDate(dateFrom)
                .toDate(dateTo)
                .itemTypes(List.of("Financial"))
                .build();
        LegacyMajorCreditorHistoryResponse legacyResponse =
            LegacyMajorCreditorHistoryResponse.builder().version(BigInteger.valueOf(7L)).build();
        GetMajorCreditorHistoryResponse mappedResponse = GetMajorCreditorHistoryResponse.builder()
            .version(BigInteger.valueOf(7))
            .build();

        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(FeatureFlags.RELEASE_1B_1_1,
            FeatureFlags.RELEASE_1B_1_1_ENABLED_PROPERTY, false)).thenReturn(false);

        when(gatewayService.postToGateway(
            GET_MAJOR_CREDITOR_ACCOUNT_HISTORY,
            LegacyMajorCreditorHistoryResponse.class,
            expectedRequest,
            null
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse));
        when(historyRequestMapper.toLegacyRequest(123L, dateFrom, dateTo, List.of("Financial")))
            .thenReturn(expectedRequest);
        when(historyResponseMapper.toOpal(legacyResponse)).thenReturn(mappedResponse);

        GetMajorCreditorHistoryResponse result =
            legacyMajorCreditorAccountService.getHistory(123L, dateFrom, dateTo, List.of("note"));

        assertEquals(mappedResponse, result);
        verify(historyRequestMapper).toLegacyRequest(123L, dateFrom, dateTo, List.of("Financial"));
        verify(gatewayService).postToGateway(
            GET_MAJOR_CREDITOR_ACCOUNT_HISTORY,
            LegacyMajorCreditorHistoryResponse.class,
            expectedRequest,
            null
        );
    }

    @Test
    void getHistory_postsLegacyRequestMapsResponseAndAllowsNoteItemTypeInR1B11Mode() {
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 1, 31);
        GetMajorCreditorAccountHistoryLegacyRequest expectedRequest =
            GetMajorCreditorAccountHistoryLegacyRequest.builder()
                .creditorAccountId("123")
                .fromDate(dateFrom)
                .toDate(dateTo)
                .itemTypes(List.of("Note"))
                .build();
        LegacyMajorCreditorHistoryResponse legacyResponse =
            LegacyMajorCreditorHistoryResponse.builder().version(BigInteger.valueOf(7L)).build();
        GetMajorCreditorHistoryResponse mappedResponse = GetMajorCreditorHistoryResponse.builder()
            .version(BigInteger.valueOf(7))
            .build();

        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(FeatureFlags.RELEASE_1B_1_1,
            FeatureFlags.RELEASE_1B_1_1_ENABLED_PROPERTY, false)).thenReturn(true);

        when(gatewayService.postToGateway(
            GET_MAJOR_CREDITOR_ACCOUNT_HISTORY,
            LegacyMajorCreditorHistoryResponse.class,
            expectedRequest,
            null
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse));
        when(historyRequestMapper.toLegacyRequest(123L, dateFrom, dateTo, List.of("Note")))
            .thenReturn(expectedRequest);
        when(historyResponseMapper.toOpal(legacyResponse)).thenReturn(mappedResponse);

        GetMajorCreditorHistoryResponse result =
            legacyMajorCreditorAccountService.getHistory(123L, dateFrom, dateTo, List.of("Note"));

        assertEquals(mappedResponse, result);
        verify(historyRequestMapper).toLegacyRequest(123L, dateFrom, dateTo, List.of("Note"));
        verify(gatewayService).postToGateway(
            GET_MAJOR_CREDITOR_ACCOUNT_HISTORY,
            LegacyMajorCreditorHistoryResponse.class,
            expectedRequest,
            null
        );
    }

    @Test
    void getHistory_handlesGatewayExceptionResponse() {
        GetMajorCreditorAccountHistoryLegacyRequest expectedRequest =
            GetMajorCreditorAccountHistoryLegacyRequest.builder()
                .creditorAccountId("123")
                .itemTypes(List.of("Financial"))
                .build();

        when(gatewayService.postToGateway(
            GET_MAJOR_CREDITOR_ACCOUNT_HISTORY,
            LegacyMajorCreditorHistoryResponse.class,
            expectedRequest,
            null
        )).thenReturn(new GatewayService.Response<>(
            HttpStatus.INTERNAL_SERVER_ERROR,
            LegacyMajorCreditorHistoryResponse.builder().build(),
            null,
            new RuntimeException("Gateway error")
        ));
        when(historyRequestMapper.toLegacyRequest(123L, null, null, List.of("Financial")))
            .thenReturn(expectedRequest);

        HttpServerErrorException exception = assertThrows(
            HttpServerErrorException.class,
            () -> legacyMajorCreditorAccountService.getHistory(123L, null, null, List.of("Financial"))
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        verify(historyRequestMapper).toLegacyRequest(123L, null, null, List.of("Financial"));
        verifyNoInteractions(historyResponseMapper);
    }

    @Test
    void getHistory_handlesGatewayClientErrorResponse() {
        GetMajorCreditorAccountHistoryLegacyRequest expectedRequest =
            GetMajorCreditorAccountHistoryLegacyRequest.builder()
                .creditorAccountId("123")
                .itemTypes(List.of("Financial"))
                .build();

        when(gatewayService.postToGateway(
            GET_MAJOR_CREDITOR_ACCOUNT_HISTORY,
            LegacyMajorCreditorHistoryResponse.class,
            expectedRequest,
            null
        )).thenReturn(new GatewayService.Response<>(
            HttpStatus.NOT_FOUND,
            LegacyMajorCreditorHistoryResponse.builder().build(),
            null,
            new RuntimeException("Gateway error")
        ));
        when(historyRequestMapper.toLegacyRequest(123L, null, null,
            List.of("Financial"))).thenReturn(expectedRequest);

        HttpClientErrorException exception = assertThrows(
            HttpClientErrorException.class,
            () -> legacyMajorCreditorAccountService.getHistory(123L, null, null, List.of("Financial"))
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(historyRequestMapper).toLegacyRequest(123L, null, null, List.of("Financial"));
        verifyNoInteractions(historyResponseMapper);
    }

    @Test
    void getAtAGlance_handlesGatewayExceptionResponse() {
        GetMajorCreditorAccountAtAGlanceLegacyResponse legacyResponse = atAGlanceLegacyResponse();
        GetMajorCreditorAccountAtAGlanceLegacyRequest expectedRequest = atAGlanceLegacyRequest();

        when(gatewayService.postToGateway(
            GET_MAJOR_CREDITOR_ACCOUNT_AT_A_GLANCE,
            GetMajorCreditorAccountAtAGlanceLegacyResponse.class,
            expectedRequest,
            null
        )).thenReturn(new GatewayService.Response<>(
            HttpStatus.INTERNAL_SERVER_ERROR,
            legacyResponse,
            null,
            new RuntimeException("Gateway error")
        ));

        HttpServerErrorException exception = assertThrows(
            HttpServerErrorException.class,
            () -> legacyMajorCreditorAccountService.getAtAGlance(123L)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        verifyNoInteractions(atAGlanceResponseMapper);
    }

    private GetMajorCreditorAccountHeaderSummaryLegacyResponse legacyResponse() {
        return GetMajorCreditorAccountHeaderSummaryLegacyResponse.builder()
            .majorCreditor(GetMajorCreditorAccountHeaderSummaryLegacyResponse.MajorCreditorLegacy.builder()
                               .creditorAccountId(123L)
                               .accountVersion(BigInteger.valueOf(7L))
                               .build())
            .businessUnitDetails(BusinessUnitSummary.builder()
                                     .businessUnitId("46")
                                     .businessUnitName("Test BU")
                                     .build())
            .build();
    }

    private GetMajorCreditorAccountAtAGlanceLegacyResponse atAGlanceLegacyResponse() {
        return GetMajorCreditorAccountAtAGlanceLegacyResponse.builder()
            .majorCreditor(MajorCreditorLegacy.builder()
                .creditorAccountId(123L)
                .creditorAccountVersion(BigInteger.valueOf(7))
                .name("Major Creditor Test Ltd")
                .build())
            .build();
    }

    private GetMajorCreditorAccountHeaderSummaryLegacyRequest legacyRequest() {
        return GetMajorCreditorAccountHeaderSummaryLegacyRequest.builder().creditorAccountId("123").build();
    }

    private GetMajorCreditorAccountAtAGlanceLegacyRequest atAGlanceLegacyRequest() {
        return GetMajorCreditorAccountAtAGlanceLegacyRequest.builder().creditorAccountId("123").build();
    }
}
