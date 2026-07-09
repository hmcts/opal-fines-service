package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.CentralFundResponse;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.generated.model.GetCentralFundResponse;
import uk.gov.hmcts.opal.generated.model.GetMajorCreditorAccountAtAGlance200Response;
import uk.gov.hmcts.opal.generated.model.GetCentralFundResponseMajorCreditor;
import uk.gov.hmcts.opal.generated.model.GetMajorCreditorAccountHeaderSummary200Response;
import uk.gov.hmcts.opal.service.CentralFundService;
import uk.gov.hmcts.opal.service.MajorCreditorAccountService;

@ExtendWith(MockitoExtension.class)
class MajorCreditorApiControllerTest {

    @Mock
    private CentralFundService centralFundService;

    @Mock
    private MajorCreditorAccountService majorCreditorAccountService;

    @InjectMocks
    private MajorCreditorApiController controller;

    @Test
    void getCentralFundByBusinessUnit_whenFeatureEnabled_returnsPayloadWithEtag() {
        GetCentralFundResponse payload = centralFundPayload();
        CentralFundResponse serviceResponse = CentralFundResponse.builder()
            .payload(payload)
            .version(BigInteger.valueOf(7))
            .build();

        when(centralFundService.getCentralFundByBusinessUnit((short) 70)).thenReturn(serviceResponse);

        ResponseEntity<GetCentralFundResponse> response =
            controller.getCentralFundByBusinessUnit((short) 70);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("\"7\"", response.getHeaders().getETag());
        assertSame(payload, response.getBody());
        verify(centralFundService).getCentralFundByBusinessUnit((short) 70);
    }

    @Test
    void getMajorCreditorAccountHeaderSummary_success() {
        GetMajorCreditorAccountHeaderSummaryResponse response = new GetMajorCreditorAccountHeaderSummaryResponse();
        response.setVersion(BigInteger.valueOf(7));

        when(majorCreditorAccountService.getHeaderSummary(123L)).thenReturn(response);

        ResponseEntity<GetMajorCreditorAccountHeaderSummary200Response> result =
            controller.getMajorCreditorAccountHeaderSummary(123L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        assertEquals("\"7\"", result.getHeaders().getETag());
        verify(majorCreditorAccountService).getHeaderSummary(123L);
    }

    @Test
    void getMajorCreditorAccountAtAGlance_success() {
        GetMajorCreditorAccountAtAGlanceResponse response = new GetMajorCreditorAccountAtAGlanceResponse();
        response.setVersion(BigInteger.valueOf(8));
        response.setMajorCreditor(new GetMajorCreditorAccountAtAGlanceResponse.MajorCreditor());

        when(majorCreditorAccountService.getAtAGlance(123L)).thenReturn(response);

        ResponseEntity<GetMajorCreditorAccountAtAGlance200Response> result =
            controller.getMajorCreditorAccountAtAGlance(123L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        assertEquals("\"8\"", result.getHeaders().getETag());
        verify(majorCreditorAccountService).getAtAGlance(123L);
    }

    private GetCentralFundResponse centralFundPayload() {
        return GetCentralFundResponse.builder()
            .majorCreditor(GetCentralFundResponseMajorCreditor.builder()
                .creditorAccountId(123L)
                .accountNumber("CF123")
                .name("Central Fund")
                .build())
            .businessUnitDetails(BusinessUnitSummaryCommon.builder()
                .businessUnitId((short) 70)
                .businessUnitName("London Collection")
                .welshSpeaking("N")
                .build())
            .build();
    }
}
