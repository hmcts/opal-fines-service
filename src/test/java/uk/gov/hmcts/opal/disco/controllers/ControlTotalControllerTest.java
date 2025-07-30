package uk.gov.hmcts.opal.disco.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.ControlTotalSearchDto;
import uk.gov.hmcts.opal.entity.ControlTotalEntity;
import uk.gov.hmcts.opal.disco.opal.ControlTotalService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControlTotalControllerTest {

    @Mock
    private ControlTotalService controlTotalService;

    @InjectMocks
    private ControlTotalController controlTotalController;

    @Test
    void testGetControlTotal_Success() {
        // Arrange
        ControlTotalEntity entity = ControlTotalEntity.builder().build();

        when(controlTotalService.getControlTotal(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<ControlTotalEntity> response = controlTotalController.getControlTotalById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(controlTotalService, times(1)).getControlTotal(any(Long.class));
    }

    @Test
    void testSearchControlTotals_Success() {
        // Arrange
        ControlTotalEntity entity = ControlTotalEntity.builder().build();
        List<ControlTotalEntity> controlTotalList = List.of(entity);

        when(controlTotalService.searchControlTotals(any())).thenReturn(controlTotalList);

        // Act
        ControlTotalSearchDto searchDto = ControlTotalSearchDto.builder().build();
        ResponseEntity<List<ControlTotalEntity>> response = controlTotalController.postControlTotalsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(controlTotalList, response.getBody());
        verify(controlTotalService, times(1)).searchControlTotals(any());
    }

}
