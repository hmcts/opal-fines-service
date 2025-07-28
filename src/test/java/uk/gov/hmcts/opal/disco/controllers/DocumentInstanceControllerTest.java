package uk.gov.hmcts.opal.disco.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.DocumentInstanceSearchDto;
import uk.gov.hmcts.opal.entity.DocumentInstanceEntity;
import uk.gov.hmcts.opal.disco.opal.DocumentInstanceService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentInstanceControllerTest {

    @Mock
    private DocumentInstanceService documentInstanceService;

    @InjectMocks
    private DocumentInstanceController documentInstanceController;

    @Test
    void testGetDocumentInstance_Success() {
        // Arrange
        DocumentInstanceEntity entity = DocumentInstanceEntity.builder().build();

        when(documentInstanceService.getDocumentInstance(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<DocumentInstanceEntity> response = documentInstanceController.getDocumentInstanceById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(documentInstanceService, times(1)).getDocumentInstance(any(Long.class));
    }

    @Test
    void testSearchDocumentInstances_Success() {
        // Arrange
        DocumentInstanceEntity entity = DocumentInstanceEntity.builder().build();
        List<DocumentInstanceEntity> documentInstanceList = List.of(entity);

        when(documentInstanceService.searchDocumentInstances(any())).thenReturn(documentInstanceList);

        // Act
        DocumentInstanceSearchDto searchDto = DocumentInstanceSearchDto.builder().build();
        ResponseEntity<List<DocumentInstanceEntity>> response = documentInstanceController
            .postDocumentInstancesSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(documentInstanceList, response.getBody());
        verify(documentInstanceService, times(1)).searchDocumentInstances(any());
    }

}
