package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyResponse.MajorCreditorLegacy;
import uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.mapper.legacy.GetMajorCreditorAccountHeaderSummaryResponseLegacyMapper;

@ExtendWith(MockitoExtension.class)
class LegacyMajorCreditorAccountServiceTest {

    @Mock
    private GatewayService gatewayService;

    @Mock
    private GetMajorCreditorAccountHeaderSummaryResponseLegacyMapper headerSummaryResponseMapper;

    @Mock
    private LegacyBusinessUnitCodeResolver legacyBusinessUnitCodeResolver;

    @InjectMocks
    private LegacyMajorCreditorAccountService legacyMajorCreditorAccountService;

    @Test
    void getHeaderSummary_postsLegacyRequestMapsResponseAndSetsVersion() {
        GetMajorCreditorAccountHeaderSummaryLegacyResponse legacyResponse = legacyResponse();
        GetMajorCreditorAccountHeaderSummaryResponse mappedResponse =
            new GetMajorCreditorAccountHeaderSummaryResponse();
        mappedResponse.setBusinessUnitDetails(new BusinessUnitSummaryCommon()
                                                 .businessUnitId("46")
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

    private GetMajorCreditorAccountHeaderSummaryLegacyResponse legacyResponse() {
        return GetMajorCreditorAccountHeaderSummaryLegacyResponse.builder()
            .majorCreditor(MajorCreditorLegacy.builder()
                               .creditorAccountId(123L)
                               .accountVersion(7L)
                               .build())
            .businessUnitDetails(BusinessUnitSummary.builder()
                                     .businessUnitId("46")
                                     .businessUnitName("Test BU")
                                     .build())
            .build();
    }

    private GetMajorCreditorAccountHeaderSummaryLegacyRequest legacyRequest() {
        return GetMajorCreditorAccountHeaderSummaryLegacyRequest.builder().creditorAccountId("123").build();
    }
}
