package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.PrisonSearchDto;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.service.opal.PrisonService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrisonControllerTest {

    @Mock
    private PrisonService prisonService;

    @InjectMocks
    private PrisonController prisonController;

    @Test
    void testGetPrison_Success() {
        // Arrange
        PrisonEntity entity = PrisonEntity.builder().build(); //some id assigned by db sequence

        when(prisonService.getPrison(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<PrisonEntity> response = prisonController.getPrisonById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(prisonService, times(1)).getPrison(any(Long.class));
    }

    @Test
    void testSearchPrisons_Success() {
        // Arrange
        PrisonEntity entity = PrisonEntity.builder().build();
        List<PrisonEntity> prisonList = List.of(entity);

        when(prisonService.searchPrisons(any())).thenReturn(prisonList);

        // Act
        PrisonSearchDto searchDto = PrisonSearchDto.builder().build();
        ResponseEntity<List<PrisonEntity>> response = prisonController.postPrisonsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(prisonList, response.getBody());
        verify(prisonService, times(1)).searchPrisons(any());
    }

}
