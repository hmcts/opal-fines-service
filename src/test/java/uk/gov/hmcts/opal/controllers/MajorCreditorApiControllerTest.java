package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.generated.model.GetMajorCreditorAccountHeaderSummary200Response;
import uk.gov.hmcts.opal.service.MajorCreditorAccountService;

@ExtendWith(MockitoExtension.class)
class MajorCreditorApiControllerTest {

    @Mock
    private MajorCreditorAccountService majorCreditorAccountService;

    @InjectMocks
    private MajorCreditorApiController controller;

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
}
