package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.TemplateMappingSearchDto;
import uk.gov.hmcts.opal.entity.TemplateMappingEntity;
import uk.gov.hmcts.opal.service.opal.TemplateMappingService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateMappingControllerTest {

    @Mock
    private TemplateMappingService templateMappingService;

    @InjectMocks
    private TemplateMappingController templateMappingController;

    @Test
    void testGetTemplateMapping_Success() {
        // Arrange
        TemplateMappingEntity entity = TemplateMappingEntity.builder().build();

        when(templateMappingService.getTemplateMapping(anyLong(), anyLong())).thenReturn(entity);

        // Act
        ResponseEntity<TemplateMappingEntity> response = templateMappingController
            .getTemplateMappingById(1L, 1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(templateMappingService, times(1)).getTemplateMapping(anyLong(), anyLong());
    }

    @Test
    void testSearchTemplateMappings_Success() {
        // Arrange
        TemplateMappingEntity entity = TemplateMappingEntity.builder().build();
        List<TemplateMappingEntity> templateMappingList = List.of(entity);

        when(templateMappingService.searchTemplateMappings(any())).thenReturn(templateMappingList);

        // Act
        TemplateMappingSearchDto searchDto = TemplateMappingSearchDto.builder().build();
        ResponseEntity<List<TemplateMappingEntity>> response = templateMappingController
            .postTemplateMappingsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(templateMappingList, response.getBody());
        verify(templateMappingService, times(1)).searchTemplateMappings(any());
    }

}
