package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountResponseMinorCreditor;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.service.MinorCreditorService;

@ExtendWith(MockitoExtension.class)
class MinorCreditorApiControllerTest {

    @Mock
    private MinorCreditorService minorCreditorService;

    @InjectMocks
    private MinorCreditorApiController minorCreditorApiController;

    @Test
    void given_validRequest_when_getMinorCreditorAccount_then_returnsOkResponse() {
        Long minorCreditorAccountId = 1L;
        MinorCreditorAccountResponse response = new MinorCreditorAccountResponse();

        when(minorCreditorService.getMinorCreditorAccount(minorCreditorAccountId)).thenReturn(response);

        ResponseEntity<MinorCreditorAccountResponseMinorCreditor> result =
            minorCreditorApiController.getMinorCreditorAccount(minorCreditorAccountId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(minorCreditorService).getMinorCreditorAccount(minorCreditorAccountId);
    }

    @Test
    void given_validRequest_when_patchMinorCreditorAccount_then_returnsOkResponse() {
        MinorCreditorAccountResponse response = new MinorCreditorAccountResponse();
        response.setCreditorAccountId(101L);
        response.setVersion(BigInteger.valueOf(2));

        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest();

        when(minorCreditorService.updateMinorCreditorAccount(101L, request, BigInteger.ONE,
            "Bearer token", "77")).thenReturn(response);

        ResponseEntity<MinorCreditorAccountResponseMinorCreditor> result =
            minorCreditorApiController.patchMinorCreditorAccount(101L, "77", "\"1\"", "Bearer token", request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("\"2\"", result.getHeaders().getETag());
        assertSame(response, result.getBody());
        verify(minorCreditorService).updateMinorCreditorAccount(101L, request, BigInteger.ONE,
            "Bearer token", "77");
    }
}
