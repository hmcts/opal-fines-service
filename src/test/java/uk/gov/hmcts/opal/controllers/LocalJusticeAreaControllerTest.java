package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.service.opal.LocalJusticeAreaService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalJusticeAreaControllerTest {

    @Mock
    private LocalJusticeAreaService localJusticeAreaService;

    @InjectMocks
    private LocalJusticeAreaController localJusticeAreaController;

    @Test
    void testGetLocalJusticeArea_Success() {
        // Arrange
        LocalJusticeAreaEntity entity = LocalJusticeAreaEntity.builder().build(); //some id assigned by db sequence

        when(localJusticeAreaService.getLocalJusticeArea(anyShort())).thenReturn(entity);

        // Act
        ResponseEntity<LocalJusticeAreaEntity> response = localJusticeAreaController.getLocalJusticeAreaById((short)1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(localJusticeAreaService, times(1)).getLocalJusticeArea(anyShort());
    }

    @Test
    void testSearchLocalJusticeAreas_Success() {
        // Arrange
        LocalJusticeAreaEntity entity = LocalJusticeAreaEntity.builder().build();
        List<LocalJusticeAreaEntity> localJusticeAreaList = List.of(entity);

        when(localJusticeAreaService.searchLocalJusticeAreas(any())).thenReturn(localJusticeAreaList);

        // Act
        LocalJusticeAreaSearchDto searchDto = LocalJusticeAreaSearchDto.builder().build();
        ResponseEntity<List<LocalJusticeAreaEntity>> response = localJusticeAreaController
            .postLocalJusticeAreasSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(localJusticeAreaList, response.getBody());
        verify(localJusticeAreaService, times(1)).searchLocalJusticeAreas(any());
    }

}
