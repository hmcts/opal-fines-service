package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.service.legacy.LegacyMajorCreditorAccountService.GET_MAJOR_CREDITOR_ACCOUNT_HEADER_SUMMARY;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyResponse.MajorCreditorLegacy;
import uk.gov.hmcts.opal.mapper.legacy.GetMajorCreditorAccountHeaderSummaryResponseLegacyMapper;

@ExtendWith(MockitoExtension.class)
class LegacyMajorCreditorAccountServiceTest {

    @Mock
    private GatewayService gatewayService;

    @Mock
    private GetMajorCreditorAccountHeaderSummaryResponseLegacyMapper headerSummaryResponseMapper;

    @InjectMocks
    private LegacyMajorCreditorAccountService legacyMajorCreditorAccountService;

    @Test
    void getHeaderSummary_postsLegacyRequestMapsResponseAndSetsVersion() {
        GetMajorCreditorAccountHeaderSummaryLegacyResponse legacyResponse =
            GetMajorCreditorAccountHeaderSummaryLegacyResponse.builder()
                .majorCreditor(MajorCreditorLegacy.builder()
                                   .creditorAccountId(123L)
                                   .accountVersion(7L)
                                   .build())
                .build();
        GetMajorCreditorAccountHeaderSummaryResponse mappedResponse =
            new GetMajorCreditorAccountHeaderSummaryResponse();

        when(gatewayService.postToGateway(
            eq(GET_MAJOR_CREDITOR_ACCOUNT_HEADER_SUMMARY),
            eq(GetMajorCreditorAccountHeaderSummaryLegacyResponse.class),
            eq(GetMajorCreditorAccountHeaderSummaryLegacyRequest.builder().creditorAccountId("123").build()),
            eq(null)
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse));
        when(headerSummaryResponseMapper.toOpal(legacyResponse)).thenReturn(mappedResponse);

        GetMajorCreditorAccountHeaderSummaryResponse result =
            legacyMajorCreditorAccountService.getHeaderSummary(123L);

        assertEquals(mappedResponse, result);
        assertEquals(BigInteger.valueOf(7), result.getVersion());

        ArgumentCaptor<GetMajorCreditorAccountHeaderSummaryLegacyRequest> requestCaptor =
            ArgumentCaptor.forClass(GetMajorCreditorAccountHeaderSummaryLegacyRequest.class);
        verify(gatewayService).postToGateway(
            eq(GET_MAJOR_CREDITOR_ACCOUNT_HEADER_SUMMARY),
            eq(GetMajorCreditorAccountHeaderSummaryLegacyResponse.class),
            requestCaptor.capture(),
            eq(null)
        );
        assertEquals("123", requestCaptor.getValue().getCreditorAccountId());
    }
}
