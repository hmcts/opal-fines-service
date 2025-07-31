package uk.gov.hmcts.opal.disco.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.ImpositionSearchDto;
import uk.gov.hmcts.opal.entity.ImpositionEntity;
import uk.gov.hmcts.opal.disco.opal.ImpositionService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImpositionControllerTest {

    @Mock
    private ImpositionService impositionService;

    @InjectMocks
    private ImpositionController impositionController;

    @Test
    void testGetImposition_Success() {
        // Arrange
        ImpositionEntity entity = ImpositionEntity.builder().build();

        when(impositionService.getImposition(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<ImpositionEntity> response = impositionController.getImpositionById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(impositionService, times(1)).getImposition(any(Long.class));
    }

    @Test
    void testSearchImpositions_Success() {
        // Arrange
        ImpositionEntity entity = ImpositionEntity.builder().build();
        List<ImpositionEntity> impositionList = List.of(entity);

        when(impositionService.searchImpositions(any())).thenReturn(impositionList);

        // Act
        ImpositionSearchDto searchDto = ImpositionSearchDto.builder().build();
        ResponseEntity<List<ImpositionEntity>> response = impositionController.postImpositionsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(impositionList, response.getBody());
        verify(impositionService, times(1)).searchImpositions(any());
    }

}
