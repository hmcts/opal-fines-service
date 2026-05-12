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
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountResponseMinorCreditor;
import uk.gov.hmcts.opal.service.MinorCreditorService;

@ExtendWith(MockitoExtension.class)
class MinorCreditorApiControllerTest {

    @Mock
    private MinorCreditorService minorCreditorService;

    @InjectMocks
    private MinorCreditorApiController minorCreditorApiController;

    @Test
    void given_validRequest_when_getMinorCreditorAccount_then_returnsOkResponse() {
        // Arrange
        MinorCreditorAccountResponse response = new MinorCreditorAccountResponse();
        response.setCreditorAccountId(101L);
        response.setVersion(BigInteger.valueOf(7));

        when(minorCreditorService.getMinorCreditorAccount(101L)).thenReturn(response);

        // Act
        ResponseEntity<MinorCreditorAccountResponseMinorCreditor> result =
            minorCreditorApiController.getMinorCreditorAccount(101L);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("\"7\"", result.getHeaders().getETag());
        assertSame(response, result.getBody());
        verify(minorCreditorService).getMinorCreditorAccount(101L);
    }
}
