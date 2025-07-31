package uk.gov.hmcts.opal.disco.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.TillSearchDto;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.disco.opal.TillService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TillControllerTest {

    @Mock
    private TillService tillService;

    @InjectMocks
    private TillController tillController;

    @Test
    void testGetTill_Success() {
        // Arrange
        TillEntity entity = TillEntity.builder().build();

        when(tillService.getTill(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<TillEntity> response = tillController.getTillById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(tillService, times(1)).getTill(any(Long.class));
    }

    @Test
    void testSearchTills_Success() {
        // Arrange
        TillEntity entity = TillEntity.builder().build();
        List<TillEntity> tillList = List.of(entity);

        when(tillService.searchTills(any())).thenReturn(tillList);

        // Act
        TillSearchDto searchDto = TillSearchDto.builder().build();
        ResponseEntity<List<TillEntity>> response = tillController.postTillsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tillList, response.getBody());
        verify(tillService, times(1)).searchTills(any());
    }

}
