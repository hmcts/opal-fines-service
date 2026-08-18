package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.service.MinorCreditorService;
import uk.gov.hmcts.opal.service.opal.OpalCreditorAccountService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinorCreditorControllerTest {

    @Mock
    MinorCreditorService minorCreditorService;

    @Mock
    OpalCreditorAccountService opalCreditorAccountService;

    @InjectMocks
    private MinorCreditorController minorCreditorController;

    @Test
    void testDeleteMinorCreditor_Success() {
        // Arrange
        when(opalCreditorAccountService.deleteCreditorAccount(anyLong(), anyBoolean())).thenReturn("OK");

        // Act
        ResponseEntity<String> responseEntity = minorCreditorController
            .deleteMinorCreditorById(444L, "if-match", Optional.of(false));

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("OK", responseEntity.getBody());
    }

}
