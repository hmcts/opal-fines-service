package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceData;
import uk.gov.hmcts.opal.service.opal.ResultService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultControllerTest {

    @Mock
    private ResultService resultService;

    @InjectMocks
    private ResultController resultController;

    @Test
    void testGetResult_Success() {
        // Arrange
        ResultReferenceData refData = new ResultReferenceData(
            "ABC", "Result AAA-BBB", "Result AAA-BBB Cy", false,
            "ResType-XX", "AAA-01234", (short)9);

        when(resultService.getResultRefDataById(any(String.class))).thenReturn(refData);

        // Act
        ResponseEntity<ResultReferenceData> response = resultController.getResultById("ABC");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(refData, response.getBody());
        verify(resultService, times(1)).getResultRefDataById(any(String.class));
    }




}
