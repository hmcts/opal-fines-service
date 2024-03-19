package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.controllers.develop.ResultController;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.ResultEntity;
import uk.gov.hmcts.opal.service.opal.ResultService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultControllerTest {

    @Mock
    private ResultService resultService;

    @InjectMocks
    private ResultController resultController;

    @Test
    void testGetResult_Success() {
        // Arrange
        ResultEntity entity = ResultEntity.builder().build();

        when(resultService.getResult(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<ResultEntity> response = resultController.getResultById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(resultService, times(1)).getResult(any(Long.class));
    }

    @Test
    void testSearchResults_Success() {
        // Arrange
        ResultEntity entity = ResultEntity.builder().build();
        List<ResultEntity> resultList = List.of(entity);

        when(resultService.searchResults(any())).thenReturn(resultList);

        // Act
        ResultSearchDto searchDto = ResultSearchDto.builder().build();
        ResponseEntity<List<ResultEntity>> response = resultController.postResultsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resultList, response.getBody());
        verify(resultService, times(1)).searchResults(any());
    }

}
