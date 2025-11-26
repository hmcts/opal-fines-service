package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.ResultDto;
import uk.gov.hmcts.opal.service.opal.ResultService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultControllerTest {

    @Mock
    private ResultService resultService;

    @InjectMocks
    private ResultController resultController;

    @Test
    void testGetResultDto_Success() {
        // Arrange: Build a simple DTO
        ResultDto dto = ResultDto.builder()
            .resultId("ABC")
            .resultTitle("Result AAA-BBB")
            .resultTitleCy("Result AAA-BBB CY")
            .build();

        when(resultService.getResultById(anyString())).thenReturn(dto);

        // Act
        ResponseEntity<ResultDto> response = resultController.getResultById("ABC");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(resultService).getResultById("ABC");
    }

    @Test
    void testGetResultDto_Success_WithSimpleJsonExpectation() {
        // Arrange
        ResultDto dto = ResultDto.builder()
            .resultId("ABC")
            .resultTitle("Some Title")
            .resultTitleCy("Welsh Title")
            .resultType("TYPE1")
            .active(true)
            .build();

        when(resultService.getResultById("ABC")).thenReturn(dto);

        // Act
        ResponseEntity<ResultDto> response = resultController.getResultById("ABC");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ABC", response.getBody().getResultId());
        assertEquals("Some Title", response.getBody().getResultTitle());
        assertEquals("Welsh Title", response.getBody().getResultTitleCy());
        assertEquals("TYPE1", response.getBody().getResultType());
        assertEquals(true, response.getBody().isActive());
    }

}
