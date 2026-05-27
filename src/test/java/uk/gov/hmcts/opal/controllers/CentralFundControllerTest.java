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
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.generated.model.GetCentralFundResponse;
import uk.gov.hmcts.opal.generated.model.GetCentralFundResponseMajorCreditor;
import uk.gov.hmcts.opal.service.CentralFundService;

@ExtendWith(MockitoExtension.class)
class CentralFundControllerTest {

    private static final String AUTH_HEADER = "Bearer test-token";

    @Mock
    private CentralFundService centralFundService;

    @InjectMocks
    private CentralFundController centralFundController;

    @Test
    void getCentralFundByBusinessUnit_whenFeatureEnabled_returnsPayloadWithEtag() {
        GetCentralFundResponse payload = centralFundPayload();
        CentralFundResponse serviceResponse = CentralFundResponse.builder()
            .payload(payload)
            .version(BigInteger.valueOf(7))
            .build();

        when(centralFundService.getCentralFundByBusinessUnit(70, AUTH_HEADER)).thenReturn(serviceResponse);

        ResponseEntity<GetCentralFundResponse> response =
            centralFundController.getCentralFundByBusinessUnit(70, AUTH_HEADER);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("\"7\"", response.getHeaders().getETag());
        assertSame(payload, response.getBody());
        verify(centralFundService).getCentralFundByBusinessUnit(70, AUTH_HEADER);
    }

    private GetCentralFundResponse centralFundPayload() {
        return GetCentralFundResponse.builder()
            .majorCreditor(GetCentralFundResponseMajorCreditor.builder()
                .creditorAccountId(123L)
                .accountNumber("CF123")
                .name("Central Fund")
                .build())
            .businessUnitDetails(BusinessUnitSummaryCommon.builder()
                .businessUnitId("70")
                .businessUnitName("London Collection")
                .welshSpeaking("N")
                .build())
            .build();
    }
}
