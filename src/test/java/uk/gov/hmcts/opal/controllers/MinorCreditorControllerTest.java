package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.Creditor;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.service.MinorCreditorService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinorCreditorControllerTest {

    @Mock
    MinorCreditorService minorCreditorService;

    @InjectMocks
    private MinorCreditorController minorCreditorController;

    @Test
    void testGetMinorCreditorAccountAtAGlance_Success() {
        // Arrange
        GetMinorCreditorAccountAtAGlanceResponse mockResponse = new GetMinorCreditorAccountAtAGlanceResponse();

        when(minorCreditorService.getMinorCreditorAtAGlance(101L)).thenReturn(mockResponse);

        // Act
        ResponseEntity<GetMinorCreditorAccountAtAGlanceResponse> responseEntity =
            minorCreditorController.getMinorCreditorsAtAGlance(101L);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
    }

}
