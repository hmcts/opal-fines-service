package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.response.GetMinorCreditorHistoryResponse;
import uk.gov.hmcts.opal.generated.model.GetMinorCreditorHistory200Response;
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
    void given_validRequest_when_getMinorCreditorHistory_then_returnsOkResponseWithETag() {
        // Arrange
        Long minorCreditorAccountId = 1L;
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 1, 31);
        List<String> itemTypes = List.of("amendment", "note");
        String authorization = "Bearer token";
        GetMinorCreditorHistory200Response payload = new GetMinorCreditorHistory200Response()
            .historyItems(List.of());
        GetMinorCreditorHistoryResponse response = GetMinorCreditorHistoryResponse.builder()
            .payload(payload)
            .version(BigInteger.valueOf(3))
            .build();

        when(minorCreditorService.getMinorCreditorHistory(
            minorCreditorAccountId, dateFrom, dateTo, itemTypes, authorization)).thenReturn(response);

        // Act
        ResponseEntity<GetMinorCreditorHistory200Response> result =
            minorCreditorApiController.getMinorCreditorHistory(
                minorCreditorAccountId, dateFrom, dateTo, itemTypes, authorization);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("\"3\"", result.getHeaders().getETag());
        assertSame(payload, result.getBody());
        verify(minorCreditorService).getMinorCreditorHistory(
            minorCreditorAccountId, dateFrom, dateTo, itemTypes, authorization);
    }

    @Test
    void getMinorCreditorHistory_whenServiceThrows_propagatesException() {
        // Arrange
        Long minorCreditorAccountId = 1L;
        RuntimeException expected = new RuntimeException("history failed");
        when(minorCreditorService.getMinorCreditorHistory(
            minorCreditorAccountId, null, null, null, "Bearer token")).thenThrow(expected);

        // Act
        RuntimeException result = assertThrows(
            RuntimeException.class,
            () -> minorCreditorApiController.getMinorCreditorHistory(
                minorCreditorAccountId, null, null, null, "Bearer token")
        );

        // Assert
        assertSame(expected, result);
        verify(minorCreditorService).getMinorCreditorHistory(
            minorCreditorAccountId, null, null, null, "Bearer token");
    }

    @Test
    void given_validRequest_when_patchMinorCreditorAccount_then_returnsOkResponse() {
        MinorCreditorAccountResponse response = new MinorCreditorAccountResponse();
        response.setCreditorAccountId(101L);
        response.setVersion(BigInteger.valueOf(2));

        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest();

        when(minorCreditorService.updateMinorCreditorAccount(101L, request, BigInteger.ONE, "77")).thenReturn(response);

        ResponseEntity<MinorCreditorAccountResponseMinorCreditor> result =
            minorCreditorApiController.patchMinorCreditorAccount(101L, "77", "\"1\"", request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("\"2\"", result.getHeaders().getETag());
        assertSame(response, result.getBody());
        verify(minorCreditorService).updateMinorCreditorAccount(101L, request, BigInteger.ONE, "77");
    }
}
