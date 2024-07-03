package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.ResultDocumentSearchDto;
import uk.gov.hmcts.opal.entity.ResultDocumentEntity;
import uk.gov.hmcts.opal.service.opal.ResultDocumentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultDocumentControllerTest {

    @Mock
    private ResultDocumentService resultDocumentService;

    @InjectMocks
    private ResultDocumentController resultDocumentController;

    @Test
    void testGetResultDocument_Success() {
        // Arrange
        ResultDocumentEntity entity = ResultDocumentEntity.builder().build();

        when(resultDocumentService.getResultDocument(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<ResultDocumentEntity> response = resultDocumentController.getResultDocumentById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(resultDocumentService, times(1)).getResultDocument(any(Long.class));
    }

    @Test
    void testSearchResultDocuments_Success() {
        // Arrange
        ResultDocumentEntity entity = ResultDocumentEntity.builder().build();
        List<ResultDocumentEntity> resultDocumentList = List.of(entity);

        when(resultDocumentService.searchResultDocuments(any())).thenReturn(resultDocumentList);

        // Act
        ResultDocumentSearchDto searchDto = ResultDocumentSearchDto.builder().build();
        ResponseEntity<List<ResultDocumentEntity>> response = resultDocumentController
            .postResultDocumentsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resultDocumentList, response.getBody());
        verify(resultDocumentService, times(1)).searchResultDocuments(any());
    }

}
