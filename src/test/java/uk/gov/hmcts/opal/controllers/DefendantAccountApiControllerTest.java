package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.generated.model.GetEnforcementStatusResponseDefendantAccount;
import uk.gov.hmcts.opal.service.DefendantAccountService;


@ExtendWith(MockitoExtension.class)
class DefendantAccountApiControllerTest {

    private static final String BEARER_TOKEN = "Bearer a_token_goes_here";

    @Mock
    private DefendantAccountService defendantAccountService;

    @InjectMocks
    private DefendantAccountApiController defendantAccountApiController;

    @Test
    void testGetDefendantAccountEnforcementStatus_Success() {
        // Arrange
        EnforcementStatus status = EnforcementStatus.newBuilder()
            .build();
        when(defendantAccountService.getEnforcementStatus(anyLong(), any()))
            .thenReturn(status);

        // Act
        ResponseEntity<GetEnforcementStatusResponseDefendantAccount> response =
            defendantAccountApiController.getEnforcementStatus(1L, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        GetEnforcementStatusResponseDefendantAccount body = response.getBody();
        assertEquals(status, body);
    }

}
