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
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.service.MajorCreditorAccountService;

@ExtendWith(MockitoExtension.class)
class MajorCreditorAccountControllerTest {

    private static final String AUTH_HEADER = "Bearer token";

    @Mock
    private MajorCreditorAccountService majorCreditorAccountService;

    @InjectMocks
    private MajorCreditorAccountController majorCreditorAccountController;

    @Test
    void getAtAGlance_returnsResponseWithEtag() {
        GetMajorCreditorAccountAtAGlanceResponse response = GetMajorCreditorAccountAtAGlanceResponse.builder()
            .version(BigInteger.ONE)
            .build();
        when(majorCreditorAccountService.getAtAGlance(101L, AUTH_HEADER)).thenReturn(response);

        ResponseEntity<GetMajorCreditorAccountAtAGlanceResponse> result =
            majorCreditorAccountController.getAtAGlance(101L, AUTH_HEADER);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        assertEquals("\"1\"", result.getHeaders().getETag());
        verify(majorCreditorAccountService).getAtAGlance(101L, AUTH_HEADER);
    }
}
