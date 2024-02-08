package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.DocumentSearchDto;
import uk.gov.hmcts.opal.entity.DocumentEntity;
import uk.gov.hmcts.opal.service.opal.DocumentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private DocumentController documentController;

    @Test
    void testGetDocument_Success() {
        // Arrange
        DocumentEntity entity = DocumentEntity.builder().build(); //some id assigned by db sequence

        when(documentService.getDocument(any(String.class))).thenReturn(entity);

        // Act
        ResponseEntity<DocumentEntity> response = documentController.getDocumentById("ID1");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(documentService, times(1)).getDocument(any(String.class));
    }

    @Test
    void testSearchDocuments_Success() {
        // Arrange
        DocumentEntity entity = DocumentEntity.builder().build();
        List<DocumentEntity> documentList = List.of(entity);

        when(documentService.searchDocuments(any())).thenReturn(documentList);

        // Act
        DocumentSearchDto searchDto = DocumentSearchDto.builder().build();
        ResponseEntity<List<DocumentEntity>> response = documentController.postDocumentsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(documentList, response.getBody());
        verify(documentService, times(1)).searchDocuments(any());
    }

}
