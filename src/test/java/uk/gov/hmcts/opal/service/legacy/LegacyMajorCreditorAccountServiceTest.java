package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.service.legacy.LegacyMajorCreditorAccountService.GET_MAJOR_CREDITOR_ACCOUNT_AT_A_GLANCE;
import static uk.gov.hmcts.opal.service.legacy.LegacyMajorCreditorAccountService.GET_MAJOR_CREDITOR_ACCOUNT_HEADER_SUMMARY;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountAtAGlanceLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountAtAGlanceLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountAtAGlanceLegacyResponse.MajorCreditorLegacy;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyResponse;
import uk.gov.hmcts.opal.mapper.legacy.GetMajorCreditorAccountAtAGlanceResponseLegacyMapper;
import uk.gov.hmcts.opal.mapper.legacy.GetMajorCreditorAccountHeaderSummaryResponseLegacyMapper;

@ExtendWith(MockitoExtension.class)
class LegacyMajorCreditorAccountServiceTest {

    @Mock
    private GatewayService gatewayService;

    @Mock
    private GetMajorCreditorAccountAtAGlanceResponseLegacyMapper atAGlanceResponseMapper;

    @Mock
    private GetMajorCreditorAccountHeaderSummaryResponseLegacyMapper headerSummaryResponseMapper;

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

        GetMajorCreditorAccountHeaderSummaryLegacyRequest expectedRequest = legacyRequest();

        when(gatewayService.postToGateway(
            GET_MAJOR_CREDITOR_ACCOUNT_HEADER_SUMMARY,
            GetMajorCreditorAccountHeaderSummaryLegacyResponse.class,
            expectedRequest,
            null
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse));
        when(headerSummaryResponseMapper.toOpal(legacyResponse)).thenReturn(mappedResponse);

        GetMajorCreditorAccountHeaderSummaryResponse result =
            legacyMajorCreditorAccountService.getHeaderSummary(123L);

        assertEquals(mappedResponse, result);
        assertEquals(BigInteger.valueOf(7), result.getVersion());

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
                               .accountVersion(7L)
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
