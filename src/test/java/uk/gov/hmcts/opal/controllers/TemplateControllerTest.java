package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.TemplateSearchDto;
import uk.gov.hmcts.opal.entity.TemplateEntity;
import uk.gov.hmcts.opal.service.opal.TemplateService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateControllerTest {

    @Mock
    private TemplateService templateService;

    @InjectMocks
    private TemplateController templateController;

    @Test
    void testGetTemplate_Success() {
        // Arrange
        TemplateEntity entity = TemplateEntity.builder().build();

        when(templateService.getTemplate(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<TemplateEntity> response = templateController.getTemplateById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(templateService, times(1)).getTemplate(any(Long.class));
    }

    @Test
    void testSearchTemplates_Success() {
        // Arrange
        TemplateEntity entity = TemplateEntity.builder().build();
        List<TemplateEntity> templateList = List.of(entity);

        when(templateService.searchTemplates(any())).thenReturn(templateList);

        // Act
        TemplateSearchDto searchDto = TemplateSearchDto.builder().build();
        ResponseEntity<List<TemplateEntity>> response = templateController.postTemplatesSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(templateList, response.getBody());
        verify(templateService, times(1)).searchTemplates(any());
    }

}
